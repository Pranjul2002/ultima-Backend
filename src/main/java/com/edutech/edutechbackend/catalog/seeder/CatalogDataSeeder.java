package com.edutech.edutechbackend.catalog.seeder;

import com.edutech.edutechbackend.catalog.entity.Chapter;
import com.edutech.edutechbackend.catalog.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CatalogDataSeeder
 * ─────────────────────────────────────────────────────────────────────────────
 * Seeds catalog chapters for the Products / Books pages.
 *
 * ⚠️  This seeder does NOT create Test entities.
 *     The 3 free dummy tests (shown on every user's dashboard) are seeded
 *     exclusively by DataInitializer.  Keeping them separate ensures the
 *     dashboard shows exactly 3 tests — never 100+.
 *
 * Runs once on startup — skips if chapters already exist.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogDataSeeder implements ApplicationRunner {

    private final ChapterRepository chapterRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (chapterRepository.count() > 0) {
            log.info("CatalogDataSeeder: chapters already present — skipping.");
            return;
        }

        log.info("CatalogDataSeeder: seeding catalog chapters…");
        seedAllChapters();
        log.info("CatalogDataSeeder: done.");
    }

    // ── Main seed ──────────────────────────────────────────────────────────────

    private void seedAllChapters() {

        // ════════════════════════════════ CLASS 10 ════════════════════════════

        seedBook("ncert-physics-class-10", List.of(
            ch("light-reflection",    1, "Light – Reflection and Refraction",           "Laws of reflection, spherical mirrors, refraction, lens formula.",                  "~45 min", 20, false),
            ch("human-eye",           2, "The Human Eye and the Colourful World",        "Structure of eye, defects of vision, atmospheric refraction, dispersion.",           "~40 min", 18, false),
            ch("electricity",         3, "Electricity",                                  "Ohm's law, resistance, series/parallel circuits, heating effect.",                   "~55 min", 22, false),
            ch("magnetic-effects",    4, "Magnetic Effects of Electric Current",         "Magnetic field, electromagnetic induction, electric motor and generator.",           "~48 min", 20, false),
            ch("sources-of-energy",   5, "Sources of Energy",                            "Conventional and non-conventional sources, solar energy, nuclear energy.",           "~35 min", 16, false)
        ));

        seedBook("ncert-chemistry-class-10", List.of(
            ch("chemical-reactions",      1, "Chemical Reactions and Equations",          "Balancing equations, types of reactions, oxidation and reduction.",              "~42 min", 18, false),
            ch("acids-bases-salts",       2, "Acids, Bases and Salts",                    "Properties, pH scale, neutralisation, salts and their uses.",                   "~45 min", 20, false),
            ch("metals-nonmetals",        3, "Metals and Non-metals",                     "Physical and chemical properties, reactivity series, corrosion.",                "~48 min", 18, false),
            ch("carbon-compounds",        4, "Carbon and its Compounds",                  "Covalent bonding, homologous series, soaps and detergents.",                     "~50 min", 20, false),
            ch("periodic-classification", 5, "Periodic Classification of Elements",       "Döbereiner, Newlands, Mendeleev and the Modern periodic table.",                 "~38 min", 12, false)
        ));

        seedBook("ncert-biology-class-10", List.of(
            ch("life-processes",       1, "Life Processes",                               "Nutrition, respiration, transportation and excretion in plants and animals.",    "~50 min", 18, false),
            ch("control-coordination", 2, "Control and Coordination",                    "Nervous system, reflex actions, hormones in plants and animals.",                 "~45 min", 16, false),
            ch("reproduction",         3, "How do Organisms Reproduce?",                 "Asexual and sexual reproduction in plants, animals and humans.",                  "~48 min", 16, false),
            ch("heredity-evolution",   4, "Heredity and Evolution",                      "Mendel's laws, sex determination, evolution and natural selection.",              "~50 min", 18, false),
            ch("our-environment",      5, "Our Environment",                             "Ecosystems, food chains, ozone layer, waste management.",                        "~38 min", 14, false),
            ch("natural-resources",    6, "Management of Natural Resources",             "Conservation of forests, water, coal, petroleum and sustainable development.",    "~35 min", 10, false)
        ));

        seedBook("ncert-maths-class-10", List.of(
            ch("real-numbers",              1,  "Real Numbers",                                  "Euclid's division lemma, fundamental theorem of arithmetic, irrational numbers.",  "~40 min", 10, false),
            ch("polynomials",               2,  "Polynomials",                                   "Zeros of a polynomial, relationship between zeros and coefficients.",               "~38 min",  8, false),
            ch("linear-equations",          3,  "Pair of Linear Equations in Two Variables",     "Graphical and algebraic methods, substitution, elimination.",                      "~50 min", 12, false),
            ch("quadratic-equations",       4,  "Quadratic Equations",                           "Standard form, factorisation, completing the square, discriminant.",               "~48 min", 10, false),
            ch("arithmetic-progressions",   5,  "Arithmetic Progressions",                       "nth term, sum of first n terms, applications.",                                    "~42 min", 10, false),
            ch("triangles",                 6,  "Triangles",                                     "Similarity, Basic Proportionality Theorem, Pythagoras theorem.",                   "~52 min", 12, false),
            ch("coordinate-geometry",       7,  "Coordinate Geometry",                           "Distance formula, section formula, area of a triangle.",                           "~42 min", 10, false),
            ch("trigonometry-intro",        8,  "Introduction to Trigonometry",                  "Ratios, identities, values of standard angles.",                                   "~45 min", 10, false),
            ch("trigonometry-applications", 9,  "Some Applications of Trigonometry",             "Heights and distances, angles of elevation and depression.",                       "~40 min",  8, false),
            ch("circles",                  10,  "Circles",                                       "Tangent to a circle, number of tangents from a point.",                            "~38 min",  8, false),
            ch("constructions",            11,  "Constructions",                                 "Division of line segment, tangents to a circle.",                                  "~30 min",  6, false),
            ch("areas-circle",             12,  "Areas Related to Circles",                      "Perimeter and area of a circle, sector, segment.",                                 "~40 min", 10, false),
            ch("surface-volumes",          13,  "Surface Areas and Volumes",                     "Combination of solids, conversion of solids, frustum of a cone.",                  "~48 min", 12, false),
            ch("statistics",               14,  "Statistics",                                    "Mean, median, mode for grouped data, cumulative frequency.",                       "~45 min", 12, false),
            ch("probability",              15,  "Probability",                                   "Classical definition, simple problems on single events.",                          "~35 min", 12, false)
        ));

        // ════════════════════════════════ CLASS 11 ════════════════════════════

        seedBook("ncert-physics-class-11", List.of(
            ch("units-measurements",    1,  "Units and Measurements",                        "SI units, significant figures, dimensions and error analysis.",                    "~40 min",  8, false),
            ch("motion-straight-line",  2,  "Motion in a Straight Line",                     "Distance, displacement, velocity, acceleration, equations of motion.",              "~45 min",  9, false),
            ch("motion-plane",          3,  "Motion in a Plane",                             "Vectors, projectile motion, circular motion, relative velocity.",                   "~50 min", 10, false),
            ch("laws-of-motion",        4,  "Laws of Motion",                                "Newton's three laws, inertia, friction, equilibrium of forces.",                    "~52 min", 10, false),
            ch("work-energy",           5,  "Work, Energy and Power",                        "Work-energy theorem, potential energy, conservation, collisions.",                  "~48 min",  9, false),
            ch("system-of-particles",   6,  "System of Particles and Rotational Motion",     "Centre of mass, torque, angular momentum, moment of inertia.",                     "~55 min", 10, false),
            ch("gravitation-11",        7,  "Gravitation",                                   "Kepler's laws, universal gravitation, escape velocity, satellites.",                "~45 min",  8, false),
            ch("mechanical-solids",     8,  "Mechanical Properties of Solids",               "Stress, strain, elastic moduli, Hooke's law.",                                     "~38 min",  7, false),
            ch("mechanical-fluids",     9,  "Mechanical Properties of Fluids",               "Pressure, Bernoulli's principle, viscosity, surface tension.",                     "~45 min",  8, false),
            ch("thermal-properties",   10,  "Thermal Properties of Matter",                  "Heat capacity, calorimetry, change of state, heat transfer.",                      "~42 min",  8, false),
            ch("thermodynamics-11",    11,  "Thermodynamics",                                "Zeroth, first and second laws, heat engines, entropy.",                            "~50 min",  9, false),
            ch("kinetic-theory",       12,  "Kinetic Theory",                                "Molecular nature of matter, kinetic theory of gases, specific heats.",             "~40 min",  8, false),
            ch("oscillations",         13,  "Oscillations",                                  "Simple harmonic motion, energy in SHM, damped oscillations.",                      "~48 min", 10, false),
            ch("waves",                14,  "Waves",                                         "Transverse and longitudinal waves, standing waves, Doppler effect.",                "~50 min", 10, false),
            ch("physical-world",       15,  "Physical World",                                "Scope of physics, fundamental forces, nature of physical laws.",                   "~25 min",  0, true)
        ));

        seedBook("ncert-chemistry-class-11", List.of(
            ch("basic-concepts",       1,  "Some Basic Concepts of Chemistry",              "Mole concept, stoichiometry, laws of chemical combination.",                       "~42 min",  9, false),
            ch("structure-of-atom",    2,  "Structure of Atom",                             "Atomic models, quantum numbers, orbitals, electronic configuration.",              "~50 min", 10, false),
            ch("periodic-table-11",    3,  "Classification of Elements and Periodicity",    "Modern periodic law, trends in properties, blocks of elements.",                   "~42 min",  8, false),
            ch("chemical-bonding",     4,  "Chemical Bonding and Molecular Structure",      "Ionic, covalent bonding, VSEPR, hybridisation, MO theory.",                       "~55 min", 10, false),
            ch("states-of-matter",     5,  "States of Matter",                              "Gaseous state, gas laws, liquefaction, liquid state.",                             "~45 min",  8, false),
            ch("thermodynamics-c11",   6,  "Thermodynamics",                                "System, enthalpy, Hess's law, entropy, Gibbs energy.",                            "~50 min",  9, false),
            ch("equilibrium-11",       7,  "Equilibrium",                                   "Chemical equilibrium, Le Chatelier's principle, ionic equilibrium.",               "~52 min", 10, false),
            ch("redox",                8,  "Redox Reactions",                               "Oxidation state, balancing redox equations, electrochemical series.",              "~40 min",  8, false),
            ch("hydrogen",             9,  "Hydrogen",                                      "Position in periodic table, preparation, properties, water.",                     "~35 min",  7, false),
            ch("s-block",             10,  "The s-Block Elements",                          "Alkali and alkaline earth metals, their compounds.",                               "~40 min",  8, false),
            ch("p-block-11",          11,  "The p-Block Elements (Group 13–14)",            "Boron, carbon families — properties and compounds.",                              "~42 min",  8, false),
            ch("organic-basics",      12,  "Organic Chemistry – Basic Principles",          "Nomenclature, isomerism, reaction mechanisms, inductive effect.",                  "~48 min",  9, false),
            ch("hydrocarbons",        13,  "Hydrocarbons",                                  "Alkanes, alkenes, alkynes, benzene, conformations.",                              "~50 min", 10, false),
            ch("environmental-chem",  14,  "Environmental Chemistry",                       "Atmospheric pollution, water pollution, soil pollution.",                         "~30 min",  0, true)
        ));

        seedBook("ncert-maths-class-11", List.of(
            ch("sets",                      1,  "Sets",                                          "Types of sets, Venn diagrams, operations on sets, De Morgan's laws.",             "~40 min",  8, false),
            ch("relations-functions-11",    2,  "Relations and Functions",                       "Ordered pairs, Cartesian product, types of functions.",                           "~42 min",  8, false),
            ch("trigonometric-functions",   3,  "Trigonometric Functions",                       "Radian measure, identities, values, graphs of trig functions.",                   "~52 min", 10, false),
            ch("complex-numbers",           4,  "Complex Numbers and Quadratic Equations",       "Imaginary unit, algebra of complex numbers, Argand plane.",                       "~45 min",  9, false),
            ch("linear-inequalities",       5,  "Linear Inequalities",                           "Algebraic and graphical solutions of linear inequalities.",                       "~38 min",  8, false),
            ch("permutations-combinations", 6,  "Permutations and Combinations",                 "Fundamental principle, nPr, nCr, applications.",                                  "~45 min",  9, false),
            ch("binomial-theorem",          7,  "Binomial Theorem",                              "Binomial expansion, general term, middle term.",                                   "~40 min",  8, false),
            ch("sequences-series",          8,  "Sequences and Series",                          "AP, GP, AM, GM, sum of special series.",                                          "~48 min",  9, false),
            ch("straight-lines",            9,  "Straight Lines",                                "Slope, various forms of equation, distance between lines.",                       "~45 min",  9, false),
            ch("conic-sections",           10,  "Conic Sections",                                "Circle, parabola, ellipse, hyperbola — standard equations.",                      "~52 min", 10, false),
            ch("3d-intro",                 11,  "Introduction to Three-Dimensional Geometry",    "Coordinate axes, distance formula, section formula in 3D.",                      "~38 min",  8, false),
            ch("limits-derivatives",       12,  "Limits and Derivatives",                        "Intuitive idea of limit, algebra of limits, derivatives.",                        "~50 min",  9, false),
            ch("statistics-11",            13,  "Statistics",                                    "Measures of dispersion, mean deviation, variance, standard deviation.",           "~42 min",  8, false),
            ch("probability-11",           14,  "Probability",                                   "Random experiment, sample space, events, axiomatic approach.",                    "~40 min",  8, false),
            ch("mathematical-reasoning",   15,  "Mathematical Reasoning",                        "Statements, connectives, validation of statements.",                              "~30 min",  0, true),
            ch("mathematical-induction",   16,  "Principle of Mathematical Induction",           "Process of the proof by induction, motivating applications.",                     "~30 min",  0, true)
        ));

        seedBook("ncert-biology-class-11", List.of(
            ch("living-world",           1,  "The Living World",                              "What is living, taxonomy, systematic and binomial nomenclature.",                 "~35 min",  5, false),
            ch("biological-class",       2,  "Biological Classification",                     "Five kingdom classification, Monera, Protista, Fungi.",                           "~40 min",  6, false),
            ch("plant-kingdom",          3,  "Plant Kingdom",                                 "Algae, bryophytes, pteridophytes, gymnosperms, angiosperms.",                     "~45 min",  6, false),
            ch("animal-kingdom",         4,  "Animal Kingdom",                                "Basis of classification, different phyla and their characteristics.",             "~45 min",  6, false),
            ch("morphology-flowering",   5,  "Morphology of Flowering Plants",                "Root, stem, leaf, flower, fruit, seed — structure and modification.",             "~48 min",  6, false),
            ch("anatomy-flowering",      6,  "Anatomy of Flowering Plants",                   "Tissue systems, anatomy of root, stem, and leaf.",                               "~45 min",  5, false),
            ch("structural-animals",     7,  "Structural Organisation in Animals",            "Tissues, organs of earthworm, cockroach and frog.",                              "~48 min",  5, false),
            ch("cell-unit",              8,  "Cell: The Unit of Life",                        "Prokaryotic and eukaryotic cell, cell organelles.",                              "~50 min",  6, false),
            ch("biomolecules-11",        9,  "Biomolecules",                                  "Chemical constituents of cell — carbohydrates, proteins, lipids, nucleic acids.","~48 min",  5, false),
            ch("cell-cycle",            10,  "Cell Cycle and Cell Division",                  "Cell cycle, mitosis, meiosis and their significance.",                           "~45 min",  5, false),
            ch("transport-plants",      11,  "Transport in Plants",                           "Means of transport, water potential, transpiration, phloem loading.",            "~42 min",  5, false),
            ch("mineral-nutrition",     12,  "Mineral Nutrition",                             "Essential mineral elements, mechanism of absorption, nitrogen cycle.",           "~38 min",  5, false),
            ch("photosynthesis",        13,  "Photosynthesis in Higher Plants",               "Light reactions, Calvin cycle, photorespiration, C4 pathway.",                   "~52 min",  6, false),
            ch("respiration-plants",    14,  "Respiration in Plants",                         "Glycolysis, fermentation, aerobic respiration, energy relations.",               "~48 min",  5, false),
            ch("plant-growth",          15,  "Plant Growth and Development",                  "Growth regulators, seed germination, dormancy, vernalisation.",                  "~40 min",  5, false),
            ch("digestion",             16,  "Digestion and Absorption",                      "Human alimentary canal, digestion, absorption and assimilation.",                "~45 min",  5, false),
            ch("breathing",             17,  "Breathing and Exchange of Gases",               "Respiratory organs, mechanism of breathing, transport of gases.",                "~42 min",  5, false),
            ch("body-fluids",           18,  "Body Fluids and Circulation",                   "Blood, lymph, human circulatory system, ECG, disorders.",                        "~48 min",  5, false),
            ch("excretion",             19,  "Excretory Products and their Elimination",      "Modes of excretion, human excretory system, kidney function.",                   "~45 min",  5, false),
            ch("locomotion",            20,  "Locomotion and Movement",                       "Types of movement, skeletal system, muscle contraction, disorders.",             "~45 min",  5, false),
            ch("neural-control",        21,  "Neural Control and Coordination",               "Neuron, reflex arc, human brain, sensory organs.",                               "~48 min",  5, false),
            ch("chemical-coordination", 22,  "Chemical Coordination and Integration",         "Endocrine glands, hormones and their regulation.",                               "~40 min",  0, true)
        ));

        // ════════════════════════════════ CLASS 12 ════════════════════════════

        seedBook("ncert-physics-class-12", List.of(
            ch("electric-charges",         1,  "Electric Charges and Fields",               "Coulomb's law, electric field, Gauss's theorem and field lines.",                "~45 min", 12, false),
            ch("electrostatic-potential",  2,  "Electrostatic Potential and Capacitance",   "Potential energy, capacitors, dielectrics and energy stored.",                   "~50 min", 10, false),
            ch("current-electricity",      3,  "Current Electricity",                       "Ohm's law, drift velocity, Kirchhoff's laws and Wheatstone bridge.",             "~55 min", 14, false),
            ch("moving-charges",           4,  "Moving Charges and Magnetism",              "Biot-Savart law, Ampere's law, cyclotron and galvanometers.",                    "~50 min", 11, false),
            ch("magnetism-matter",         5,  "Magnetism and Matter",                      "Bar magnet, Earth's magnetism, dia/para/ferromagnetic materials.",               "~40 min",  8, false),
            ch("em-induction",             6,  "Electromagnetic Induction",                 "Faraday's laws, Lenz's law, motional EMF and inductance.",                       "~48 min", 10, false),
            ch("alternating-current",      7,  "Alternating Current",                       "AC circuits, impedance, resonance, LC oscillations and transformers.",           "~52 min", 11, false),
            ch("em-waves",                 8,  "Electromagnetic Waves",                     "Maxwell's equations, EM spectrum, properties of EM waves.",                      "~35 min",  7, false),
            ch("ray-optics",               9,  "Ray Optics and Optical Instruments",        "Reflection, refraction, lenses, microscope and telescope.",                      "~60 min", 15, false),
            ch("wave-optics",             10,  "Wave Optics",                               "Huygens principle, interference, diffraction and polarisation.",                 "~48 min", 10, false),
            ch("dual-nature",             11,  "Dual Nature of Radiation and Matter",       "Photoelectric effect, de Broglie wavelength and Davisson-Germer.",               "~42 min",  9, false),
            ch("atoms",                   12,  "Atoms",                                     "Rutherford model, Bohr model, atomic spectra and hydrogen.",                     "~40 min",  8, false),
            ch("nuclei",                  13,  "Nuclei",                                    "Binding energy, radioactivity, nuclear fission and fusion.",                     "~45 min",  8, false),
            ch("semiconductor-devices",   14,  "Semiconductor Electronics",                 "p-n junction, diodes, transistors, logic gates and digital circuits.",           "~55 min", 15, false),
            ch("communication-systems",   15,  "Communication Systems",                     "Modulation, transmission, bandwidth, antennas and Internet.",                    "~35 min",  0, true)
        ));

        seedBook("ncert-chemistry-class-12", List.of(
            ch("solid-state",         1,  "The Solid State",                                "Crystal systems, defects, electrical and magnetic properties.",                   "~42 min",  8, false),
            ch("solutions-ch",        2,  "Solutions",                                      "Types, concentration, colligative properties, osmosis.",                          "~48 min",  9, false),
            ch("electrochemistry",    3,  "Electrochemistry",                               "Cell potential, Nernst equation, electrolysis, batteries.",                       "~50 min", 10, false),
            ch("chemical-kinetics",   4,  "Chemical Kinetics",                              "Rate laws, order, activation energy, Arrhenius equation.",                        "~45 min",  9, false),
            ch("surface-chemistry",   5,  "Surface Chemistry",                              "Adsorption, catalysis, colloids and emulsions.",                                  "~38 min",  7, false),
            ch("isolation-elements",  6,  "General Principles of Isolation of Elements",    "Occurrence, extraction, refining of metals.",                                     "~35 min",  6, false),
            ch("p-block-12-1",        7,  "The p-Block Elements (Part I)",                  "Groups 15-16: nitrogen family and oxygen family.",                                "~50 min",  9, false),
            ch("p-block-12-2",        8,  "The p-Block Elements (Part II)",                 "Groups 17-18: halogens and noble gases.",                                         "~42 min",  8, false),
            ch("d-f-block",           9,  "The d- and f-Block Elements",                    "Transition metals, lanthanides, actinides.",                                      "~48 min",  9, false),
            ch("coordination",       10,  "Coordination Compounds",                         "IUPAC nomenclature, isomerism, bonding, CFT.",                                    "~55 min", 12, false),
            ch("haloalkanes",        11,  "Haloalkanes and Haloarenes",                     "Preparation, reactions, uses of halogen compounds.",                              "~45 min",  8, false),
            ch("alcohols",           12,  "Alcohols, Phenols and Ethers",                   "Classification, reactions, acidity.",                                             "~48 min",  9, false),
            ch("aldehydes",          13,  "Aldehydes, Ketones and Carboxylic Acids",        "Preparation, reactions, uses.",                                                   "~52 min", 10, false),
            ch("amines",             14,  "Amines",                                         "Classification, preparation, properties of amines.",                              "~40 min",  7, false),
            ch("biomolecules-12",    15,  "Biomolecules",                                   "Carbohydrates, proteins, lipids, nucleic acids, vitamins.",                       "~45 min",  8, false),
            ch("polymers",           16,  "Polymers",                                       "Classification, preparation, uses of synthetic polymers.",                        "~35 min",  0, true)
        ));

        seedBook("ncert-biology-class-12", List.of(
            ch("sexual-reproduction",   1,  "Sexual Reproduction in Flowering Plants",      "Flower structure, pollination, fertilisation, seed development.",                "~50 min", 10, false),
            ch("human-reproduction",    2,  "Human Reproduction",                           "Male and female reproductive systems, gametogenesis, fertilisation.",            "~48 min",  9, false),
            ch("reproductive-health",   3,  "Reproductive Health",                          "Population control, contraceptives, STIs, MTP.",                                "~38 min",  7, false),
            ch("principles-heredity",   4,  "Principles of Inheritance and Variation",      "Mendel's laws, dominance, sex determination, chromosomal disorders.",            "~55 min", 12, false),
            ch("molecular-inheritance", 5,  "Molecular Basis of Inheritance",               "DNA structure, replication, transcription, translation, gene regulation.",       "~60 min", 14, false),
            ch("evolution",             6,  "Evolution",                                    "Origin of life, Darwin's theory, evidence and speciation.",                      "~45 min",  9, false),
            ch("human-health",          7,  "Human Health and Disease",                     "Immunity, vaccines, allergies, cancer, drugs and alcohol.",                      "~50 min", 10, false),
            ch("food-production",       8,  "Strategies for Enhancement in Food Production","Plant breeding, tissue culture, animal husbandry.",                              "~40 min",  8, false),
            ch("microbes",              9,  "Microbes in Human Welfare",                    "Microbes in food, biogas, sewage, biocontrol.",                                  "~38 min",  7, false),
            ch("biotechnology",        10,  "Biotechnology – Principles and Processes",     "Recombinant DNA, cloning, PCR, electrophoresis.",                                "~52 min", 10, false),
            ch("biotech-applications", 11,  "Biotechnology and its Applications",           "GM organisms, biopharmaceuticals, gene therapy, bioethics.",                     "~45 min",  9, false),
            ch("organisms-environment",12,  "Organisms and Populations",                    "Ecology, population attributes, growth, interactions.",                          "~48 min",  9, false),
            ch("ecosystem",            13,  "Ecosystem",                                    "Productivity, decomposition, energy flow, ecological pyramids.",                 "~45 min",  9, false),
            ch("biodiversity",         14,  "Biodiversity and Conservation",                "Types of biodiversity, hotspots, threats, in-situ and ex-situ.",                 "~42 min",  8, false),
            ch("environmental-issues", 15,  "Environmental Issues",                         "Pollution types, greenhouse effect, ozone depletion.",                           "~40 min",  0, true)
        ));

        seedBook("ncert-maths-class-12", List.of(
            ch("relations-functions-12",  1,  "Relations and Functions",                    "Types of relations, functions, composition and invertible functions.",            "~45 min", 12, false),
            ch("inverse-trig",            2,  "Inverse Trigonometric Functions",             "Domains, ranges and properties of inverse trig functions.",                      "~40 min", 10, false),
            ch("matrices",                3,  "Matrices",                                   "Operations, transpose, symmetric, invertible matrices.",                         "~50 min", 14, false),
            ch("determinants",            4,  "Determinants",                               "Properties, cofactors, applications in area and system of equations.",            "~52 min", 13, false),
            ch("continuity",              5,  "Continuity and Differentiability",            "Continuity, chain rule, implicit, logarithmic differentiation.",                 "~55 min", 14, false),
            ch("applications-derivatives",6,  "Applications of Derivatives",                "Rate of change, increasing/decreasing, tangents, maxima/minima.",                "~55 min", 14, false),
            ch("integrals",               7,  "Integrals",                                  "Indefinite integrals, substitution, partial fractions, by parts.",               "~60 min", 16, false),
            ch("applications-integrals",  8,  "Applications of Integrals",                  "Area under curves between lines, circles, parabolas and ellipses.",              "~45 min", 10, false),
            ch("differential-equations",  9,  "Differential Equations",                     "Order, degree, formation, variable separable, linear DEs.",                      "~52 min", 13, false),
            ch("vectors",                10,  "Vector Algebra",                              "Types, operations, scalar and vector products.",                                 "~48 min", 12, false),
            ch("3d-geometry",            11,  "Three Dimensional Geometry",                  "Direction cosines, lines, planes in 3D space.",                                  "~50 min", 12, false),
            ch("linear-programming",     12,  "Linear Programming",                          "Feasible region, objective function, corner point method.",                      "~40 min", 10, false),
            ch("probability-12",         13,  "Probability",                                 "Conditional probability, Bayes' theorem, random variables, distributions.",      "~55 min", 15, false)
        ));
    }

    // ── Per-book seeder ────────────────────────────────────────────────────────

    private void seedBook(String bookSlug, List<ChapterSeed> seeds) {
        for (ChapterSeed s : seeds) {
            Chapter chapter = Chapter.builder()
                    .chapterId(s.id())
                    .bookSlug(bookSlug)
                    .number(s.number())
                    .title(s.title())
                    .description(s.description())
                    .duration(s.duration())
                    .questionCount(s.comingSoon() ? 0 : s.questionCount())
                    .comingSoon(s.comingSoon())
                    .linkedTestId(null)   // ← no test linked from catalog seeder
                    .build();
            chapterRepository.save(chapter);
        }
        log.info("  Seeded {} chapters for {}", seeds.size(), bookSlug);
    }

    // ── Seed records ──────────────────────────────────────────────────────────

    private record ChapterSeed(
            String  id,
            int     number,
            String  title,
            String  description,
            String  duration,
            int     questionCount,
            boolean comingSoon
    ) {}

    private static ChapterSeed ch(String id, int number, String title,
                                   String description, String duration,
                                   int qCount, boolean comingSoon) {
        return new ChapterSeed(id, number, title, description, duration, qCount, comingSoon);
    }
}
