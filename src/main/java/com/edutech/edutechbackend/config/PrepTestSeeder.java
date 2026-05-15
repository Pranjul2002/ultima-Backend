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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PrepTestSeeder
 * ──────────────────────────────────────────────────────────────────────────────
 * Seeds 7 FREE competitive-exam tests into the database:
 *
 *   JEE  → Physics (15q), Chemistry (15q), Mathematics (15q)
 *   NEET → Physics (15q), Biology (15q), Chemistry (15q)
 *   GATE → General Engineering Test (65q)
 *
 * All tests are FREE (isPaid = false), accessible to every authenticated user.
 * The seeder is idempotent — it checks by title and skips if already present.
 *
 * Run order @Order(2) — runs after DataInitializer (@Order(1) / CommandLineRunner).
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class PrepTestSeeder implements ApplicationRunner {

    private final UserRepository        userRepository;
    private final TestRepository        testRepository;
    private final QuestionRepository    questionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final PasswordEncoder       passwordEncoder;

    private static final String PREP_EMAIL    = "prep.seed@edutech.internal";
    private static final String PREP_USERNAME = "EduTech Prep Seed";
    private static final String PREP_PASSWORD = "PrepSeed@2025!";

    // ── Title constants (used as unique idempotency keys) ─────────────────────
    public static final String JEE_PHYSICS    = "JEE Physics Test";
    public static final String JEE_CHEMISTRY  = "JEE Chemistry Test";
    public static final String JEE_MATHS      = "JEE Mathematics Test";
    public static final String NEET_PHYSICS   = "NEET Physics Test";
    public static final String NEET_BIO       = "NEET Biology Test";
    public static final String NEET_CHEMISTRY = "NEET Chemistry Test";
    public static final String GATE_TEST      = "GATE Engineering Test";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User seed = getOrCreateSeedMentor();

        seedTest(seed, JEE_PHYSICS,    jeePhysicsQuestions());
        seedTest(seed, JEE_CHEMISTRY,  jeeChemistryQuestions());
        seedTest(seed, JEE_MATHS,      jeeMathsQuestions());
        seedTest(seed, NEET_PHYSICS,   neetPhysicsQuestions());
        seedTest(seed, NEET_BIO,       neetBiologyQuestions());
        seedTest(seed, NEET_CHEMISTRY, neetChemistryQuestions());
        seedTest(seed, GATE_TEST,      gateQuestions());

        log.info("✅  PrepTestSeeder: 7 competitive tests ready (JEE ×3, NEET ×3, GATE ×1).");
    }

    // ── Seed mentor ───────────────────────────────────────────────────────────

    private User getOrCreateSeedMentor() {
        return userRepository.findByEmail(PREP_EMAIL).orElseGet(() -> {
            User mentor = User.builder()
                    .email(PREP_EMAIL)
                    .username(PREP_USERNAME)
                    .password(passwordEncoder.encode(PREP_PASSWORD))
                    .role(Role.MENTOR)
                    .build();
            return userRepository.save(mentor);
        });
    }

    // ── Seed one test (idempotent) ────────────────────────────────────────────

    private void seedTest(User mentor, String title, List<QuestionSeed> seeds) {
        boolean exists = testRepository.findAll().stream()
                .anyMatch(t -> t.getTitle().equals(title));
        if (exists) {
            log.info("ℹ️  PrepTestSeeder: '{}' already exists — skipping.", title);
            return;
        }

        Test test = testRepository.save(
                Test.builder()
                        .title(title)
                        .isPaid(false)
                        .price(0.0)
                        .subject(null)
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
        log.info("✅  PrepTestSeeder: seeded '{}' with {} questions.", title, seeds.size());
    }

    // ── Data carrier ──────────────────────────────────────────────────────────

    private record QuestionSeed(
            String questionText,
            String optionA, String optionB, String optionC, String optionD,
            String correctAnswer,
            int marks
    ) {}

    // ══════════════════════════════════════════════════════════════════════════
    // JEE PHYSICS  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> jeePhysicsQuestions() {
        return List.of(
                new QuestionSeed("A particle moves in a circle of radius 2 m with uniform speed 4 m/s. Its centripetal acceleration is:",
                        "2 m/s²", "4 m/s²", "8 m/s²", "16 m/s²", "C", 4),
                new QuestionSeed("Two bodies of masses m and 4m are placed at distance r. The gravitational force between them is F. If distance is doubled, the force becomes:",
                        "F/2", "F/4", "2F", "4F", "B", 4),
                new QuestionSeed("A spring of spring constant k is stretched by x. The potential energy stored is:",
                        "kx", "kx²", "kx²/2", "2kx²", "C", 4),
                new QuestionSeed("The work done by a force of 10 N over a displacement of 5 m at 60° to the direction of force is:",
                        "25 J", "50 J", "43.3 J", "86.6 J", "A", 4),
                new QuestionSeed("For a projectile launched at 45° to the horizontal, the ratio of maximum height to horizontal range is:",
                        "1:1", "1:2", "1:4", "2:1", "C", 4),
                new QuestionSeed("The escape velocity from Earth's surface is approximately:",
                        "7.9 km/s", "11.2 km/s", "15.8 km/s", "3.0 km/s", "B", 4),
                new QuestionSeed("A wave has frequency 500 Hz and wavelength 0.6 m. Its speed is:",
                        "100 m/s", "200 m/s", "300 m/s", "600 m/s", "C", 4),
                new QuestionSeed("The dimensional formula of angular momentum is:",
                        "[ML²T⁻¹]", "[MLT⁻¹]", "[ML²T⁻²]", "[ML⁻¹T⁻¹]", "A", 4),
                new QuestionSeed("A resistor of 4 Ω and a capacitor of 3 µF are connected in series to an AC source. The impedance is (f = 1000/6π Hz):",
                        "4 Ω", "5 Ω", "7 Ω", "3 Ω", "B", 4),
                new QuestionSeed("A photon of wavelength 400 nm has energy (h = 6.63×10⁻³⁴ J·s, c = 3×10⁸ m/s):",
                        "3.1 eV", "2.7 eV", "4.9 eV", "1.5 eV", "A", 4),
                new QuestionSeed("Bernoulli's principle is based on conservation of:",
                        "Mass", "Momentum", "Energy", "Angular momentum", "C", 4),
                new QuestionSeed("The ratio of translational to rotational kinetic energy of a rolling sphere is:",
                        "5:2", "2:5", "7:5", "5:7", "A", 4),
                new QuestionSeed("An object is placed at 30 cm from a convex lens of focal length 10 cm. The image distance is:",
                        "10 cm", "15 cm", "20 cm", "25 cm", "B", 4),
                new QuestionSeed("Which of the following electromagnetic waves has the highest frequency?",
                        "Radio waves", "Infrared", "Ultraviolet", "Gamma rays", "D", 4),
                new QuestionSeed("The half-life of a radioactive element is 10 years. After 30 years the fraction remaining is:",
                        "1/8", "1/4", "1/16", "1/2", "A", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JEE CHEMISTRY  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> jeeChemistryQuestions() {
        return List.of(
                new QuestionSeed("The number of moles of O₂ required to burn 1 mole of C₃H₈ completely is:",
                        "3", "4", "5", "6", "C", 4),
                new QuestionSeed("Which quantum number determines the shape of an orbital?",
                        "Principal (n)", "Azimuthal (l)", "Magnetic (m)", "Spin (s)", "B", 4),
                new QuestionSeed("The hybridisation of carbon in ethyne (C₂H₂) is:",
                        "sp³", "sp²", "sp", "sp³d", "C", 4),
                new QuestionSeed("pH of a 0.001 M HCl solution is:",
                        "1", "2", "3", "4", "C", 4),
                new QuestionSeed("The IUPAC name of CH₃–CH(OH)–CH₃ is:",
                        "1-propanol", "2-propanol", "Propan-1-ol", "Methyl ethanol", "B", 4),
                new QuestionSeed("Which of the following is a colligative property?",
                        "Optical rotation", "Refractive index", "Osmotic pressure", "Viscosity", "C", 4),
                new QuestionSeed("Avogadro's number is approximately:",
                        "6.022 × 10²²", "6.022 × 10²³", "6.022 × 10²⁴", "6.022 × 10²⁵", "B", 4),
                new QuestionSeed("In an electrochemical cell, oxidation occurs at the:",
                        "Cathode", "Anode", "Salt bridge", "Electrolyte", "B", 4),
                new QuestionSeed("The reagent used to test for aldehyde group is:",
                        "Tollen's reagent", "Lucas test", "Baeyer's reagent", "Fehling's solution only", "A", 4),
                new QuestionSeed("According to VSEPR, the shape of NH₃ is:",
                        "Trigonal planar", "Tetrahedral", "Pyramidal (trigonal pyramidal)", "Linear", "C", 4),
                new QuestionSeed("Which catalyst is used in the Haber process?",
                        "V₂O₅", "Fe", "Ni", "Pt", "B", 4),
                new QuestionSeed("The standard electrode potential of SHE (Standard Hydrogen Electrode) is:",
                        "1.0 V", "0.5 V", "0.0 V", "-1.0 V", "C", 4),
                new QuestionSeed("Which of the following is an example of a lyophilic colloid?",
                        "Gold sol", "Starch sol", "Arsenic sulphide sol", "Silver sol", "B", 4),
                new QuestionSeed("Rate law for a first-order reaction is r = k[A]. If [A] doubles, rate:",
                        "Remains same", "Doubles", "Quadruples", "Halves", "B", 4),
                new QuestionSeed("The major product of Markovnikov addition of HBr to propene is:",
                        "1-bromopropane", "2-bromopropane", "Allyl bromide", "Propan-2-ol", "B", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JEE MATHEMATICS  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> jeeMathsQuestions() {
        return List.of(
                new QuestionSeed("The value of lim(x→0) [sin x / x] is:",
                        "0", "∞", "1", "Undefined", "C", 4),
                new QuestionSeed("If f(x) = x³ − 3x, then f'(x) at x = 2 is:",
                        "6", "9", "3", "12", "B", 4),
                new QuestionSeed("∫ e^x dx equals:",
                        "e^x + C", "xe^x + C", "e^x / x + C", "x·e^x − e^x + C", "A", 4),
                new QuestionSeed("The sum of the infinite geometric series 1 + 1/2 + 1/4 + … is:",
                        "1", "1.5", "2", "3", "C", 4),
                new QuestionSeed("The equation of a circle with centre (1,2) and radius 3 is:",
                        "(x+1)²+(y+2)²=9", "(x−1)²+(y−2)²=9", "(x−1)²+(y−2)²=3", "x²+y²=9", "B", 4),
                new QuestionSeed("If A = [[1,2],[3,4]], then det(A) is:",
                        "−2", "2", "10", "−10", "A", 4),
                new QuestionSeed("The number of ways to arrange 5 distinct objects in a row is:",
                        "25", "60", "100", "120", "D", 4),
                new QuestionSeed("The complex number i²⁰⁰ equals:",
                        "1", "−1", "i", "−i", "A", 4),
                new QuestionSeed("If sin θ = 3/5 and θ is in the first quadrant, cos θ is:",
                        "4/5", "3/4", "5/4", "5/3", "A", 4),
                new QuestionSeed("The discriminant of 2x² − 5x + 3 = 0 is:",
                        "1", "−1", "25", "49", "A", 4),
                new QuestionSeed("The slope of the line 3x − 4y + 8 = 0 is:",
                        "3/4", "4/3", "−3/4", "−4/3", "A", 4),
                new QuestionSeed("The value of ⁸C₃ is:",
                        "28", "40", "56", "70", "C", 4),
                new QuestionSeed("∫₀¹ x² dx equals:",
                        "1/2", "1/3", "2/3", "1", "B", 4),
                new QuestionSeed("The angle between vectors A = (1,0) and B = (0,1) is:",
                        "0°", "45°", "90°", "180°", "C", 4),
                new QuestionSeed("If log₂ 8 = x, then x is:",
                        "2", "3", "4", "8", "B", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NEET PHYSICS  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> neetPhysicsQuestions() {
        return List.of(
                new QuestionSeed("The SI unit of electric charge is:",
                        "Ampere", "Coulomb", "Volt", "Farad", "B", 4),
                new QuestionSeed("A body of mass 2 kg falls from height 5 m. Its kinetic energy just before hitting the ground (g = 10 m/s²) is:",
                        "50 J", "100 J", "25 J", "200 J", "B", 4),
                new QuestionSeed("The focal length of a plane mirror is:",
                        "Zero", "Infinity", "Negative", "Positive finite", "B", 4),
                new QuestionSeed("Which of the following is a vector quantity?",
                        "Speed", "Distance", "Temperature", "Displacement", "D", 4),
                new QuestionSeed("The pressure at depth h in a liquid of density ρ is:",
                        "ρgh", "ρg/h", "ρ/gh", "gh/ρ", "A", 4),
                new QuestionSeed("Sound travels fastest in:",
                        "Air", "Water", "Vacuum", "Steel", "D", 4),
                new QuestionSeed("The phenomenon of light bending around corners is called:",
                        "Reflection", "Refraction", "Diffraction", "Interference", "C", 4),
                new QuestionSeed("The momentum of a body is p. Its kinetic energy in terms of p and mass m is:",
                        "p/2m", "p²/2m", "2mp", "p²m/2", "B", 4),
                new QuestionSeed("Ohm's law is V = IR. If resistance is doubled and voltage remains constant, current:",
                        "Doubles", "Remains same", "Halves", "Quadruples", "C", 4),
                new QuestionSeed("The frequency of a wave with time period 0.01 s is:",
                        "10 Hz", "100 Hz", "1000 Hz", "0.1 Hz", "B", 4),
                new QuestionSeed("Which mirror is used in vehicle rear-view mirrors?",
                        "Concave", "Plane", "Convex", "Parabolic", "C", 4),
                new QuestionSeed("The first law of thermodynamics is essentially a statement of:",
                        "Conservation of momentum", "Conservation of charge", "Conservation of energy", "Conservation of mass", "C", 4),
                new QuestionSeed("Refractive index of glass is 1.5. Speed of light in glass is (c = 3×10⁸ m/s):",
                        "1×10⁸ m/s", "2×10⁸ m/s", "4.5×10⁸ m/s", "3×10⁸ m/s", "B", 4),
                new QuestionSeed("A capacitor of 5 µF is charged to 100 V. Energy stored is:",
                        "0.025 J", "0.05 J", "0.25 J", "2.5 J", "A", 4),
                new QuestionSeed("Nuclear fission involves:",
                        "Joining of light nuclei", "Splitting of heavy nucleus", "Beta decay", "Electron capture", "B", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NEET BIOLOGY  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> neetBiologyQuestions() {
        return List.of(
                new QuestionSeed("Which organelle is called the powerhouse of the cell?",
                        "Nucleus", "Ribosome", "Mitochondria", "Golgi body", "C", 4),
                new QuestionSeed("DNA replication is:",
                        "Conservative", "Semi-conservative", "Dispersive", "Both A and C", "B", 4),
                new QuestionSeed("The structural and functional unit of kidney is:",
                        "Alveolus", "Nephron", "Hepatocyte", "Neuron", "B", 4),
                new QuestionSeed("Photosynthesis takes place in:",
                        "Mitochondria", "Chloroplast", "Nucleus", "Endoplasmic reticulum", "B", 4),
                new QuestionSeed("Which blood group is universal donor?",
                        "A", "B", "AB", "O", "D", 4),
                new QuestionSeed("The genetic material in most organisms is:",
                        "RNA", "Protein", "DNA", "Lipid", "C", 4),
                new QuestionSeed("Krebs cycle occurs in:",
                        "Cytoplasm", "Nucleus", "Mitochondrial matrix", "Chloroplast", "C", 4),
                new QuestionSeed("Which hormone is known as the 'fight or flight' hormone?",
                        "Insulin", "Thyroxine", "Adrenaline", "Glucagon", "C", 4),
                new QuestionSeed("The process of converting glucose to pyruvate is called:",
                        "Krebs cycle", "Glycolysis", "Fermentation", "Oxidative phosphorylation", "B", 4),
                new QuestionSeed("How many chromosomes does a normal human somatic cell contain?",
                        "23", "44", "46", "48", "C", 4),
                new QuestionSeed("Which part of the brain controls body temperature and hunger?",
                        "Cerebrum", "Cerebellum", "Hypothalamus", "Medulla oblongata", "C", 4),
                new QuestionSeed("Transpiration in plants occurs mainly through:",
                        "Roots", "Stomata", "Lenticels", "Cuticle", "B", 4),
                new QuestionSeed("The enzyme that unwinds the DNA double helix during replication is:",
                        "Ligase", "Polymerase", "Helicase", "Gyrase", "C", 4),
                new QuestionSeed("Which vitamin is essential for blood clotting?",
                        "Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K", "D", 4),
                new QuestionSeed("Mendel's Law of Segregation states that:",
                        "Two alleles blend in offspring", "Alleles for different traits assort independently",
                        "Each gamete carries only one allele for each trait", "Dominant alleles always expressed", "C", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NEET CHEMISTRY  (15 questions · 4 marks each)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> neetChemistryQuestions() {
        return List.of(
                new QuestionSeed("Which of the following is the most electronegative element?",
                        "Oxygen", "Chlorine", "Fluorine", "Nitrogen", "C", 4),
                new QuestionSeed("The molecular formula of glucose is:",
                        "C₆H₁₂O₅", "C₆H₁₂O₆", "C₁₂H₂₂O₁₁", "C₆H₁₀O₅", "B", 4),
                new QuestionSeed("The number of sigma bonds in ethane (C₂H₆) is:",
                        "5", "6", "7", "8", "C", 4),
                new QuestionSeed("Which of the following is an alkali metal?",
                        "Calcium", "Magnesium", "Potassium", "Aluminium", "C", 4),
                new QuestionSeed("The process of gaining electrons is called:",
                        "Oxidation", "Reduction", "Hydration", "Sublimation", "B", 4),
                new QuestionSeed("Which gas is produced when zinc reacts with dilute HCl?",
                        "O₂", "Cl₂", "H₂", "SO₂", "C", 4),
                new QuestionSeed("The molar mass of NaCl is:",
                        "40 g/mol", "48 g/mol", "58.5 g/mol", "74.5 g/mol", "C", 4),
                new QuestionSeed("Which bond is present in diamond?",
                        "Ionic", "Metallic", "Van der Waals", "Covalent", "D", 4),
                new QuestionSeed("The pH of a neutral solution at 25°C is:",
                        "0", "7", "14", "1", "B", 4),
                new QuestionSeed("Which functional group is present in alcohols?",
                        "−COOH", "−CHO", "−OH", "−NH₂", "C", 4),
                new QuestionSeed("Esterification is a reaction between:",
                        "Acid and base", "Acid and alcohol", "Alkene and acid", "Aldehyde and alcohol", "B", 4),
                new QuestionSeed("The atomic number of carbon is:",
                        "4", "6", "8", "12", "B", 4),
                new QuestionSeed("Which catalyst is used in Ostwald's process for nitric acid production?",
                        "Fe", "Ni", "Pt/Rh", "V₂O₅", "C", 4),
                new QuestionSeed("The shape of water molecule (H₂O) is:",
                        "Linear", "Tetrahedral", "Bent (V-shaped)", "Trigonal planar", "C", 4),
                new QuestionSeed("Which of the following polymers is formed by addition polymerisation?",
                        "Nylon", "Bakelite", "Polyethylene", "Dacron", "C", 4)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GATE ENGINEERING  (65 questions · 1 mark each for Q1-25, 2 marks for Q26-65)
    // ══════════════════════════════════════════════════════════════════════════
    private List<QuestionSeed> gateQuestions() {
        // Q1–25: General Aptitude + Engineering Maths (1 mark each)
        // Q26–65: Core Engineering / CS (2 marks each)
        return List.of(
                // ─ General Aptitude (Q1–10) ─
                new QuestionSeed("If 6 workers complete a job in 12 days, 4 workers will complete the same job in:",
                        "18 days", "16 days", "20 days", "24 days", "A", 1),
                new QuestionSeed("The next term in the series 3, 6, 11, 18, 27 is:",
                        "36", "38", "40", "38", "B", 1),
                new QuestionSeed("Choose the word that is most similar in meaning to 'EPHEMERAL':",
                        "Eternal", "Transient", "Robust", "Ancient", "B", 1),
                new QuestionSeed("A is twice as old as B. Five years ago A was three times older than B. A's current age is:",
                        "15", "20", "25", "30", "B", 1),
                new QuestionSeed("The ratio of boys to girls in a class is 4:5. If total students = 54, number of girls is:",
                        "24", "28", "30", "32", "C", 1),
                new QuestionSeed("'Eloquent' most nearly means:",
                        "Loud", "Fluent and expressive", "Silent", "Confused", "B", 1),
                new QuestionSeed("A boat travels 30 km upstream in 3 hrs and 30 km downstream in 2 hrs. Speed of current:",
                        "2.5 km/h", "5 km/h", "3 km/h", "4 km/h", "A", 1),
                new QuestionSeed("Fill in the blank: 'Neither the manager nor the employees _____ present.'",
                        "was", "were", "is", "are", "B", 1),
                new QuestionSeed("If APPLE = 50, MANGO = 53, then GRAPE = ?",
                        "47", "50", "49", "52", "C", 1),
                new QuestionSeed("In a 100-metre race, A beats B by 10 m and B beats C by 10 m. A beats C by:",
                        "19 m", "20 m", "21 m", "18 m", "A", 1),

                // ─ Engineering Mathematics (Q11–25) ─
                new QuestionSeed("The rank of matrix [[1,2],[2,4]] is:",
                        "0", "1", "2", "3", "B", 1),
                new QuestionSeed("The eigenvalues of [[2,1],[0,3]] are:",
                        "2, 3", "1, 3", "0, 2", "2, 0", "A", 1),
                new QuestionSeed("The Laplace transform of u(t) (unit step) is:",
                        "1/s²", "1/s", "s", "e^−s", "B", 1),
                new QuestionSeed("∫₀^∞ e^(−st) dt converges for:",
                        "s < 0", "s = 0", "s > 0", "All s", "C", 1),
                new QuestionSeed("The curl of a conservative vector field is:",
                        "A constant", "Non-zero", "Zero", "Infinity", "C", 1),
                new QuestionSeed("For the differential equation dy/dx + y = 0 with y(0) = 1, solution is:",
                        "y = e^x", "y = e^(−x)", "y = x", "y = 1", "B", 1),
                new QuestionSeed("P(A∪B) = P(A) + P(B) − P(A∩B). If A and B are mutually exclusive:",
                        "P(A∩B) = 1", "P(A∩B) = P(A)·P(B)", "P(A∩B) = 0", "P(A∪B) = 0", "C", 1),
                new QuestionSeed("The mean of a Poisson distribution equals its:",
                        "Standard deviation", "Variance", "Mode", "Median", "B", 1),
                new QuestionSeed("The Taylor series of e^x at x=0 starts with:",
                        "1 + x + x²/2! + …", "x + x²/2 + …", "1 − x + x² − …", "x − x²/2 + …", "A", 1),
                new QuestionSeed("A 3×3 matrix has determinant 5. If all elements are multiplied by 2, new determinant is:",
                        "10", "20", "40", "80", "C", 1),
                new QuestionSeed("The Z-transform of unit impulse δ[n] is:",
                        "z/(z−1)", "1", "1/(z−1)", "z", "B", 1),
                new QuestionSeed("A fair die is rolled twice. Probability both show 6 is:",
                        "1/6", "1/12", "1/18", "1/36", "D", 1),
                new QuestionSeed("The gradient of f(x,y) = x² + y² at (1,1) is:",
                        "(2,2)", "(1,1)", "(2,0)", "(0,2)", "A", 1),
                new QuestionSeed("The number of spanning trees of a complete graph K₄ is:",
                        "4", "8", "12", "16", "D", 1),
                new QuestionSeed("Fourier series of a periodic function decomposes it into:",
                        "Polynomials", "Sine and cosine components", "Exponentials only", "Logarithms", "B", 1),

                // ─ Digital Logic & Computer Organization (Q26–35) ─
                new QuestionSeed("The Boolean expression A·(A+B) simplifies to:",
                        "A+B", "A", "B", "AB", "B", 2),
                new QuestionSeed("The number of flip-flops needed to design a mod-16 counter is:",
                        "2", "3", "4", "5", "C", 2),
                new QuestionSeed("In 2's complement representation, −5 in 8-bit is:",
                        "11111010", "11111011", "10000101", "01111011", "B", 2),
                new QuestionSeed("NAND gate is universal because:",
                        "It has two inputs", "Any Boolean function can be implemented using only NAND gates",
                        "It outputs 1 always", "It is the simplest gate", "B", 2),
                new QuestionSeed("Which of the following is a non-volatile memory?",
                        "SRAM", "DRAM", "Cache", "ROM", "D", 2),
                new QuestionSeed("The hexadecimal equivalent of (110 1010)₂ is:",
                        "5A", "6A", "7A", "8A", "B", 2),
                new QuestionSeed("In direct mapped cache with 16 blocks and main memory of 256 blocks, each memory block maps to:",
                        "1 cache block", "16 cache blocks", "4 cache blocks", "256 cache blocks", "A", 2),
                new QuestionSeed("Pipelining improves:",
                        "Latency of a single instruction", "Throughput of instruction execution",
                        "Power consumption", "Cache hit rate", "B", 2),
                new QuestionSeed("RISC processors are characterised by:",
                        "Complex instruction set", "Many addressing modes", "Fixed instruction length and few addressing modes",
                        "Microcode-based execution", "C", 2),
                new QuestionSeed("The time to access a word in main memory is 100 ns; in cache 10 ns. Hit rate 90%. Average access time:",
                        "10 ns", "19 ns", "28 ns", "50 ns", "B", 2),

                // ─ Programming & Data Structures (Q36–45) ─
                new QuestionSeed("What is the time complexity of binary search on a sorted array of n elements?",
                        "O(n)", "O(log n)", "O(n log n)", "O(n²)", "B", 2),
                new QuestionSeed("Which data structure uses LIFO order?",
                        "Queue", "Stack", "Linked list", "Tree", "B", 2),
                new QuestionSeed("The height of a complete binary tree with 15 nodes is:",
                        "3", "4", "5", "6", "B", 2),
                new QuestionSeed("Quicksort has average-case time complexity of:",
                        "O(n)", "O(n log n)", "O(n²)", "O(log n)", "B", 2),
                new QuestionSeed("In C, sizeof(int) on a 32-bit system typically returns:",
                        "1", "2", "4", "8", "C", 2),
                new QuestionSeed("Which traversal of a BST gives elements in sorted order?",
                        "Preorder", "Postorder", "Inorder", "Level-order", "C", 2),
                new QuestionSeed("Dijkstra's algorithm finds:",
                        "Minimum spanning tree", "Shortest path from a single source",
                        "Maximum flow", "Strongly connected components", "B", 2),
                new QuestionSeed("A hash table of size 10 uses linear probing. Load factor should ideally be kept below:",
                        "0.3", "0.5", "0.7", "0.9", "C", 2),
                new QuestionSeed("Which sorting algorithm is stable and has O(n log n) worst case?",
                        "Quicksort", "Heapsort", "Merge sort", "Selection sort", "C", 2),
                new QuestionSeed("In object-oriented programming, 'encapsulation' refers to:",
                        "Inheritance of classes", "Bundling data and methods into a single unit",
                        "Polymorphism via interfaces", "Overriding parent methods", "B", 2),

                // ─ Algorithms & Theory of Computation (Q46–55) ─
                new QuestionSeed("A problem is in class NP if it:",
                        "Cannot be solved in polynomial time", "Can be solved by a non-deterministic Turing machine in polynomial time",
                        "Has no known algorithm", "Is undecidable", "B", 2),
                new QuestionSeed("The language {aⁿbⁿ | n ≥ 0} is:",
                        "Regular", "Context-free but not regular", "Context-sensitive", "Recursively enumerable only", "B", 2),
                new QuestionSeed("Which algorithm uses the greedy approach for MST?",
                        "Bellman-Ford", "Floyd-Warshall", "Kruskal's", "DFS", "C", 2),
                new QuestionSeed("DFS on a graph with V vertices and E edges has time complexity:",
                        "O(V)", "O(E)", "O(V + E)", "O(V·E)", "C", 2),
                new QuestionSeed("Amortized cost of n operations on a dynamic array is:",
                        "O(1) per operation", "O(n) per operation", "O(log n) per operation", "O(n²) total", "A", 2),
                new QuestionSeed("The pumping lemma is used to prove a language is:",
                        "Context-free", "Regular", "Not regular (or not context-free)", "Recursive", "C", 2),
                new QuestionSeed("Which of the following is undecidable?",
                        "Checking if a DFA accepts any string", "Halting problem for Turing machines",
                        "Minimising a DFA", "Checking if two DFAs accept the same language", "B", 2),
                new QuestionSeed("Floyd-Warshall finds shortest paths between:",
                        "One source and all destinations", "All pairs of vertices",
                        "Two specific vertices", "Minimum spanning tree", "B", 2),
                new QuestionSeed("The master theorem applies to recurrences of the form:",
                        "T(n) = T(n−1) + f(n)", "T(n) = aT(n/b) + f(n)",
                        "T(n) = T(n/2) + T(n/3)", "T(n) = nT(n−1)", "B", 2),
                new QuestionSeed("A regular language is closed under:",
                        "Union", "Intersection", "Complement", "All of the above", "D", 2),

                // ─ Operating Systems & Computer Networks (Q56–65) ─
                new QuestionSeed("In a system with 5 processes and 3 resource types, the Banker's algorithm is used for:",
                        "Memory management", "Deadlock avoidance", "Process scheduling", "Page replacement", "B", 2),
                new QuestionSeed("LRU page replacement policy evicts the page that was:",
                        "Used most recently", "Loaded first", "Least recently used", "Used most frequently", "C", 2),
                new QuestionSeed("TCP is a:",
                        "Connectionless, unreliable protocol", "Connection-oriented, reliable protocol",
                        "Datagram protocol", "Stateless protocol", "B", 2),
                new QuestionSeed("The OSI model layer responsible for end-to-end communication is:",
                        "Network layer", "Data link layer", "Transport layer", "Session layer", "C", 2),
                new QuestionSeed("In CSMA/CD, the minimum frame size depends on:",
                        "Network speed", "Propagation delay", "Both network speed and propagation delay",
                        "Number of nodes", "C", 2),
                new QuestionSeed("Which scheduling algorithm gives minimum average waiting time for a given set of processes?",
                        "FCFS", "Round Robin", "SJF (Shortest Job First)", "Priority scheduling", "C", 2),
                new QuestionSeed("A subnet mask of 255.255.255.0 means:",
                        "24-bit host portion", "24-bit network portion", "8-bit network portion", "16-bit network portion", "B", 2),
                new QuestionSeed("Thrashing in an OS occurs when:",
                        "CPU is idle most of the time", "Processes spend more time paging than executing",
                        "Memory is sufficient for all processes", "Disk I/O is slow", "B", 2),
                new QuestionSeed("The critical section problem solution must satisfy: mutual exclusion, progress, and:",
                        "Starvation", "Bounded waiting", "Deadlock", "Priority inversion", "B", 2),
                new QuestionSeed("IPv6 addresses are:",
                        "32 bits", "64 bits", "128 bits", "256 bits", "C", 2)
        );
    }
}