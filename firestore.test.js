const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user access: Alice cannot read Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      soberStartDate: "2026-01-01",
      points: 100,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: Alice can create and read her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      soberStartDate: "2026-11-01",
      substanceName: "Alcohol & Nicotine",
      points: 50,
      currentStreakDays: 10,
      longestStreakDays: 10,
      createdAt: new Date(),
    })
  );

  const doc = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  if (!doc.exists) {
    throw new Error("Doc should exist");
  }
});

test("Cross-user access: Alice cannot write to Bob's goals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("goals").doc("goal_1").set({
      userId: BOB_UID,
      title: "Walk 15 mins",
      period: "daily",
      targetCount: 1,
      currentProgress: 0,
      isCompleted: false,
      createdAt: new Date(),
    })
  );
});

test("Alice can create and list her own goals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("goals").doc("goal_1").set({
      userId: ALICE_UID,
      title: "Meditation & Urge Surfing",
      period: "daily",
      category: "mindfulness",
      targetCount: 1,
      currentProgress: 1,
      isCompleted: true,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("goals").get());
});

test("Alice can log daily check-in with mood and cravings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("check_ins").doc("2026-11-11").set({
      userId: ALICE_UID,
      date: "2026-11-11",
      status: "sober",
      cravingLevel: 2,
      mood: "great",
      notes: "Had a great morning walk. Urges passed in 5 minutes.",
      triggers: ["stress"],
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("check_ins").get());
});

test("Alice can receive milestone reward and create custom reward", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("rewards").doc("badge_1").set({
      userId: ALICE_UID,
      title: "1 Week Clean",
      description: "You've stayed clean for 7 straight days!",
      iconName: "emoji_events",
      badgeType: "milestone",
      pointsAwarded: 100,
      positiveReinforcement: "Your brain is actively forming new neural pathways without the substance. Great job!",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_rewards").doc("reward_coffee").set({
      userId: ALICE_UID,
      title: "Specialty Caramel Macchiato",
      costPoints: 50,
      isRedeemed: false,
      createdAt: new Date(),
    })
  );
});
