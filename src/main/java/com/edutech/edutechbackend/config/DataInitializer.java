package com.edutech.edutechbackend.config;

import com.edutech.edutechbackend.question.entity.Question;
import com.edutech.edutechbackend.question.repository.QuestionRepository;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.test.repository.TestRepository;
import com.edutech.edutechbackend.testattempt.repository.TestAttemptRepository;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.enums.Role;
import com.edutech.edutechbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataInitializer
 * ─────────────────────────────────────────────────────────────────────────────
 * Runs FIRST at startup (CommandLineRunner, before ApplicationRunner).
 *
 * Step 1 — Wipe ALL previously seeded free tests (including the 100+ tests
 *           that CatalogDataSeeder used to create per chapter).
 *           Only tests owned by the two known seed-mentor emails are removed,
 *           so real mentor-created tests are left untouched.
 *
 * Step 2 — Seed exactly 3 free dummy tests with 25 questions each.
 *           subject = null  →  NOT tied to any class (10 / 11 / 12).
 *
 * ⚠️  CatalogDataSeeder has been updated to NOT create Test entities at all,
 *     so this cleanup only needs to run once (first deploy after the fix).
 *     Subsequent restarts are fully idempotent.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository        userRepository;
    private final TestRepository        testRepository;
    private final QuestionRepository    questionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final PasswordEncoder       passwordEncoder;

    // ── Known seed-mentor e-mails (old + new) ────────────────────────────────
    private static final String SEED_EMAIL_OLD = "system@ultima.edu";
    private static final String SEED_EMAIL     = "seed.mentor@edutech.internal";
    private static final String SEED_USERNAME  = "EduTech Seed";
    private static final String SEED_PASSWORD  = "SeedMentor@2025!";

    // ── New test titles ───────────────────────────────────────────────────────
    private static final String T1 = "Free General Science Test";
    private static final String T2 = "Free Logical Reasoning Test";
    private static final String T3 = "Free Aptitude & Quantitative Test";

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void run(String... args) {
        wipeSeedMentorTests();
        User seed = getOrCreateSeedMentor();
        seedTest(seed, T1, scienceQuestions());
        seedTest(seed, T2, reasoningQuestions());
        seedTest(seed, T3, aptitudeQuestions());
        log.info("✅  DataInitializer: 3 free tests (25 q each) ready.");
    }

    // ── Delete every free test that belongs to either seed-mentor account ─────
    private void wipeSeedMentorTests() {
        for (String email : List.of(SEED_EMAIL, SEED_EMAIL_OLD)) {
            userRepository.findByEmail(email).ifPresent(mentor -> {
                List<Test> owned = testRepository.findAll().stream()
                        .filter(t -> t.getMentor() != null
                                  && t.getMentor().getId().equals(mentor.getId()))
                        .toList();
                if (!owned.isEmpty()) {
                    owned.forEach(testAttemptRepository::deleteByTest);
                    testRepository.deleteAll(owned);   // cascade removes questions
                    log.info("🗑️  DataInitializer: deleted {} tests owned by {}.", owned.size(), email);
                }
            });
        }
    }

    // ── Get-or-create seed mentor ─────────────────────────────────────────────
    private User getOrCreateSeedMentor() {
        return userRepository.findByEmail(SEED_EMAIL).orElseGet(() -> {
            User mentor = User.builder()
                    .email(SEED_EMAIL)
                    .username(SEED_USERNAME)
                    .password(passwordEncoder.encode(SEED_PASSWORD))
                    .role(Role.MENTOR)
                    .build();
            return userRepository.save(mentor);
        });
    }

    // ── Seed one test (skips if title already exists) ─────────────────────────
    private void seedTest(User mentor, String title, List<QuestionSeed> seeds) {
        boolean exists = testRepository.findAll().stream()
                .anyMatch(t -> t.getTitle().equals(title));
        if (exists) {
            log.info("ℹ️  DataInitializer: '{}' already exists — skipping.", title);
            return;
        }

        Test test = testRepository.save(
                Test.builder()
                        .title(title)
                        .isPaid(false)
                        .price(0.0)
                        .subject(null)   // ← No class / subject affiliation
                        .mentor(mentor)
                        .build()
        );

        for (QuestionSeed s : seeds) {
            questionRepository.save(
                    Question.builder()
                            .questionText(s.questionText())
                            .optionA(s.optionA())
                            .optionB(s.optionB())
                            .optionC(s.optionC())
                            .optionD(s.optionD())
                            .correctAnswer(s.correctAnswer())
                            .marks(s.marks())
                            .test(test)
                            .build()
            );
        }
        log.info("✅  DataInitializer: seeded '{}' with {} questions.", title, seeds.size());
    }

    // ── Data carrier ──────────────────────────────────────────────────────────
    private record QuestionSeed(
            String questionText,
            String optionA, String optionB, String optionC, String optionD,
            String correctAnswer,
            int marks
    ) {}

    // ══════════════════════════════════════════════════════════════════════════
    // TEST 1 — FREE GENERAL SCIENCE TEST  (25 questions)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> scienceQuestions() {
        return List.of(
            new QuestionSeed("What is the SI unit of force?",
                "Joule","Newton","Pascal","Watt","B",4),
            new QuestionSeed("Which gas is most abundant in Earth's atmosphere?",
                "Oxygen","Carbon Dioxide","Nitrogen","Argon","C",4),
            new QuestionSeed("The speed of light in vacuum is approximately:",
                "3×10⁶ m/s","3×10⁸ m/s","3×10¹⁰ m/s","3×10⁴ m/s","B",4),
            new QuestionSeed("What is the chemical formula of water?",
                "HO","H₂O₂","H₂O","OH","C",4),
            new QuestionSeed("Which planet is closest to the Sun?",
                "Venus","Earth","Mars","Mercury","D",4),
            new QuestionSeed("The process by which plants make food using sunlight is called:",
                "Respiration","Photosynthesis","Transpiration","Osmosis","B",4),
            new QuestionSeed("What is the atomic number of Hydrogen?",
                "2","0","1","3","C",4),
            new QuestionSeed("Which organ pumps blood throughout the human body?",
                "Lungs","Kidneys","Brain","Heart","D",4),
            new QuestionSeed("The unit of electric current is:",
                "Volt","Watt","Ampere","Ohm","C",4),
            new QuestionSeed("Which of the following is a renewable source of energy?",
                "Coal","Petroleum","Nuclear fuel","Solar energy","D",4),
            new QuestionSeed("Sound cannot travel through:",
                "Water","Air","Vacuum","Steel","C",4),
            new QuestionSeed("The boiling point of water at standard atmospheric pressure is:",
                "90°C","95°C","100°C","110°C","C",4),
            new QuestionSeed("Which metal is liquid at room temperature?",
                "Iron","Mercury","Aluminum","Copper","B",4),
            new QuestionSeed("DNA stands for:",
                "Deoxyrose Nucleic Acid","Deoxyribonucleic Acid","Di-Nitrogen Acid","Dinucleotide Acid","B",4),
            new QuestionSeed("The force that pulls objects towards the Earth is called:",
                "Magnetism","Friction","Gravity","Tension","C",4),
            new QuestionSeed("Which part of the cell is called the powerhouse?",
                "Nucleus","Ribosome","Mitochondria","Cell wall","C",4),
            new QuestionSeed("Ozone layer is present in which layer of the atmosphere?",
                "Troposphere","Stratosphere","Mesosphere","Thermosphere","B",4),
            new QuestionSeed("The chemical symbol for Gold is:",
                "Go","Gd","Ag","Au","D",4),
            new QuestionSeed("Which disease is caused by deficiency of Vitamin C?",
                "Rickets","Night blindness","Scurvy","Anaemia","C",4),
            new QuestionSeed("Newton's First Law of Motion is also known as the Law of:",
                "Acceleration","Gravitation","Inertia","Conservation","C",4),
            new QuestionSeed("The pH value of pure water is:",
                "0","5","7","14","C",4),
            new QuestionSeed("Which type of lens is used to correct short-sightedness (myopia)?",
                "Convex","Bifocal","Plano-convex","Concave","D",4),
            new QuestionSeed("Red blood cells are produced in the:",
                "Liver","Spleen","Bone marrow","Kidneys","C",4),
            new QuestionSeed("The hardest natural substance on Earth is:",
                "Iron","Gold","Diamond","Quartz","C",4),
            new QuestionSeed("Which gas is released during photosynthesis?",
                "Carbon dioxide","Nitrogen","Hydrogen","Oxygen","D",4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TEST 2 — FREE LOGICAL REASONING TEST  (25 questions)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> reasoningQuestions() {
        return List.of(
            new QuestionSeed("If all roses are flowers and all flowers need water, then:",
                "All water needs roses","All roses need water","Roses are not flowers","Flowers need roses","B",4),
            new QuestionSeed("Find the odd one out: Cat, Dog, Rose, Elephant",
                "Cat","Dog","Rose","Elephant","C",4),
            new QuestionSeed("What comes next in the series: 2, 4, 8, 16, ?",
                "18","24","32","36","C",4),
            new QuestionSeed("If Monday is the 1st day, what is the 15th day?",
                "Sunday","Monday","Tuesday","Wednesday","B",4),
            new QuestionSeed("A is the brother of B. B is the mother of C. What is A to C?",
                "Father","Uncle","Brother","Cousin","B",4),
            new QuestionSeed("What number comes next: 1, 4, 9, 16, 25, ?",
                "30","34","36","40","C",4),
            new QuestionSeed("In a row of students, Ram is 10th from left and 20th from right. Total students:",
                "28","29","30","31","B",4),
            new QuestionSeed("If 3 + 4 = 21 and 5 + 6 = 55, then 7 + 8 = ?",
                "85","87","99","105","C",4),
            new QuestionSeed("Choose the analogous pair: Doctor : Hospital :: Teacher : ?",
                "Book","School","Study","Knowledge","B",4),
            new QuestionSeed("Find the missing number: 5, 10, 20, 40, ?",
                "60","70","80","90","C",4),
            new QuestionSeed("Which word does NOT belong: Happy, Sad, Angry, Chair",
                "Happy","Sad","Angry","Chair","D",4),
            new QuestionSeed("If you are facing North and turn 180°, you now face:",
                "North","East","West","South","D",4),
            new QuestionSeed("What is the next term: Z, X, V, T, ?",
                "Q","R","S","P","B",4),
            new QuestionSeed("Five people sit in a row. D is to the left of A, A is to the left of B, C is to the right of B. Who is at the far left?",
                "A","B","C","D","D",4),
            new QuestionSeed("A train travels 60 km/hr. How long to travel 150 km?",
                "2 hours","2.5 hours","3 hours","3.5 hours","B",4),
            new QuestionSeed("Complete the analogy: Lion : Roar :: Snake : ?",
                "Bark","Hiss","Chirp","Growl","B",4),
            new QuestionSeed("What is 15% of 200?",
                "20","25","30","35","C",4),
            new QuestionSeed("In how many ways can letters A, B, C be arranged?",
                "3","4","6","8","C",4),
            new QuestionSeed("If today is Wednesday, what day is it after 100 days?",
                "Monday","Tuesday","Friday","Saturday","C",4),
            new QuestionSeed("Pointing to a photo, a man says 'Her mother is my mother's only daughter.' Who is in the photo?",
                "His aunt","His sister","His daughter","His mother","C",4),
            new QuestionSeed("If FIRE = 6985 and RICE = 9835, then RIFE = ?",
                "9685","9865","9658","9856","A",4),
            new QuestionSeed("A clock shows 3:15. What angle do the minute and hour hands make?",
                "0°","7.5°","90°","360°","B",4),
            new QuestionSeed("All birds can fly. Penguin is a bird. Conclusion (from premises alone):",
                "Penguin can fly (valid from given premises)","Penguin cannot fly","All birds are penguins","None","A",4),
            new QuestionSeed("Which number is wrong in: 2, 6, 12, 20, 30, 42, 56, 71?",
                "42","56","71","30","C",4),
            new QuestionSeed("A is taller than B. B is taller than C. C is taller than D. Who is the shortest?",
                "A","B","C","D","D",4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TEST 3 — FREE APTITUDE & QUANTITATIVE TEST  (25 questions)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> aptitudeQuestions() {
        return List.of(
            new QuestionSeed("What is 15% of 300?",
                "30","40","45","50","C",4),
            new QuestionSeed("The average of 5, 10, 15, 20, 25 is:",
                "10","12","15","18","C",4),
            new QuestionSeed("If a number is increased by 20% then decreased by 20%, the net change is:",
                "0%","Increase of 4%","Decrease of 4%","Increase of 2%","C",4),
            new QuestionSeed("A car travels 240 km in 4 hours. Its speed is:",
                "40 km/h","50 km/h","60 km/h","70 km/h","C",4),
            new QuestionSeed("What is the LCM of 4 and 6?",
                "12","16","18","24","A",4),
            new QuestionSeed("The square root of 144 is:",
                "11","12","13","14","B",4),
            new QuestionSeed("If 8 workers complete a job in 6 days, how many days will 4 workers take?",
                "8","10","12","14","C",4),
            new QuestionSeed("Simple interest on ₹1000 at 5% per annum for 2 years is:",
                "₹50","₹100","₹150","₹200","B",4),
            new QuestionSeed("What is 2³ × 2² equal to?",
                "2⁴","2⁵","2⁶","2⁸","B",4),
            new QuestionSeed("A shopkeeper buys an item for ₹200 and sells it for ₹250. Profit % is:",
                "20%","25%","30%","50%","B",4),
            new QuestionSeed("The HCF of 12 and 18 is:",
                "3","4","6","9","C",4),
            new QuestionSeed("If 3x = 12, then x = ?",
                "2","3","4","5","C",4),
            new QuestionSeed("Perimeter of a rectangle with length 8 cm and breadth 5 cm is:",
                "13 cm","18 cm","26 cm","40 cm","C",4),
            new QuestionSeed("The ratio 3:4 is equivalent to:",
                "6:9","9:12","6:10","4:3","B",4),
            new QuestionSeed("A train 200 m long passes a pole in 10 seconds. Speed of the train:",
                "15 m/s","18 m/s","20 m/s","25 m/s","C",4),
            new QuestionSeed("How many prime numbers are between 1 and 20?",
                "6","7","8","9","C",4),
            new QuestionSeed("Area of a circle with radius 7 cm? (π = 22/7)",
                "44 cm²","77 cm²","154 cm²","176 cm²","C",4),
            new QuestionSeed("If A:B = 2:3 and B:C = 4:5, then A:C = ?",
                "8:15","2:5","4:10","6:15","A",4),
            new QuestionSeed("Milk and water in ratio 3:1, total 40 litres. How much milk?",
                "10 L","20 L","30 L","35 L","C",4),
            new QuestionSeed("Value of (a + b)² when a = 3 and b = 2?",
                "10","20","25","30","C",4),
            new QuestionSeed("A man walks 5 km north, then 12 km east. Distance from start?",
                "7 km","13 km","15 km","17 km","B",4),
            new QuestionSeed("Time to cover 90 km at 45 km/h is:",
                "1 hour","2 hours","3 hours","4 hours","B",4),
            new QuestionSeed("Cost price of 20 articles = selling price of 16. Profit % is:",
                "10%","20%","25%","30%","C",4),
            new QuestionSeed("√(0.0081) = ?",
                "0.009","0.09","0.9","9","B",4),
            new QuestionSeed("In a class of 40 students, 60% are girls. How many boys?",
                "12","14","16","18","C",4)
        );
    }
}
