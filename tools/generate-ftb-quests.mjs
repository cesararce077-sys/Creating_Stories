import { mkdirSync, rmSync, writeFileSync } from "node:fs";
import { join, resolve, sep } from "node:path";

const roots = [
  "config/ftbquests/quests",
  "overrides/config/ftbquests/quests",
];

const MASK_63 = 0x7fffffffffffffffn;

const ANTIQUE_ATLAS = {
  id: "minecraft:book",
  components: {
    "minecraft:custom_name": '{"translate":"item.antique_atlas.atlas"}',
  },
};

function id(label) {
  let hash = 0xcbf29ce484222325n;
  for (const byte of Buffer.from(`creating-stories:${label}`, "utf8")) {
    hash ^= BigInt(byte);
    hash = (hash * 0x100000001b3n) & MASK_63;
  }
  if (hash <= 1n) {
    hash += 2n;
  }
  return hash.toString(16).toUpperCase().padStart(16, "0");
}

function quote(value) {
  return JSON.stringify(value);
}

function quoteRichText(value) {
  // FTB Library treats every & as the start of a formatting code.
  // A literal ampersand must reach its parser as \&.
  return quote(value.replaceAll("&", "\\&"));
}

function paragraphs(...parts) {
  return parts.join("\\n\\n");
}

function check(slug, title, description, x, y, options = {}) {
  return { slug, title, description, x, y, task: { type: "checkmark" }, ...options };
}

function item(slug, title, description, itemId, x, y, options = {}) {
  return { slug, title, description, x, y, icon: itemId, task: { type: "item", item: itemId }, ...options };
}

function kill(slug, title, description, entity, icon, x, y, options = {}) {
  return { slug, title, description, x, y, icon, task: { type: "kill", entity }, ...options };
}

const groups = [
  { slug: "start", title: "Start Here" },
  { slug: "engineering", title: "Engineering" },
  { slug: "magic", title: "Magic" },
  { slug: "world", title: "World & Adversaries" },
  { slug: "knowledge", title: "Knowledge & Navigation" },
];

const chapters = [
  {
    slug: "welcome",
    title: "Welcome, Wayfarer",
    group: "start",
    icon: "ftbquests:book",
    order: 0,
    quests: [
      check(
        "premise",
        "A World Worth Crossing",
        paragraphs(
          "Creating Stories is a rustic fantasy expedition pack. Build visible machinery, cross a dangerous world, study several magical traditions, and turn temporary camps into places with history.",
          "The core of the journey is Create engineering, Aeronautics airships, Truly Modular equipment, unified Ars Nouveau and Iron's spellcasting, Malum spirit-work, and exploration through Ice and Fire, Cataclysm, Mowzie's Mobs, Alex's Caves, and Deeper and Darker.",
          "Read the book as a field guide, not a checklist. Chapters explain preparations and milestones; they do not replace experimentation."
        ),
        -3,
        0,
        {
          icon: "satchels:satchel",
          reward: "satchels:satchel",
          shape: "rsquare",
          size: 1.6,
          minWidth: 320,
        }
      ),
      check(
        "rules",
        "The Rules This World Bends",
        paragraphs(
          "The End portal accepts twelve distinct Eyes chosen from sixteen bosses. Four hunts may be skipped. The Dragon opens the End and the Source; it is a campaign threshold, not final victory.",
          "Important knowledge has renewable routes, and essential structures have maps or an expensive Explorer's Compass fallback. The Antique Atlas records where you have travelled—it does not reveal an unexplored world.",
          "Death begins a recovery expedition. Your corpse keeps the loss recoverable. Magical, boss, relic, powered, and spellcaster equipment remains special instead of being flattened into ordinary modular gear."
        ),
        0,
        0,
        {
          icon: "sophisticatedbackpacks:backpack",
          reward: "sophisticatedbackpacks:backpack",
          shape: "rsquare",
          size: 1.6,
          minWidth: 320,
        }
      ),
      check(
        "objectives",
        "What You Are Building Toward",
        paragraphs(
          "Raise two persistent creations: a modular masterwork that can be repaired and rebuilt, and an airship that grows from flying tent to mobile workshop and finally a true expedition vessel.",
          "Learn three connected magical languages. Iron's handles active combat, Ars handles theory and utility, and Malum handles spirits, runes, and soul behavior. Ars and Iron's ultimately cast through one equipped spellbook and one mana pool.",
          "The long victory is mastery: a defended fortress, functioning infrastructure, apex hunts completed, and a flotilla capable of carrying the story beyond the Dragon."
        ),
        3,
        0,
        {
          icon: ANTIQUE_ATLAS,
          reward: ANTIQUE_ATLAS,
          rewardKey: "atlas-v2",
          shape: "rsquare",
          size: 1.6,
          minWidth: 320,
        }
      ),
    ],
  },
  {
    slug: "campaign_compass",
    title: "Campaign Compass",
    group: "start",
    icon: ANTIQUE_ATLAS,
    order: 1,
    quests: [
      check("prey", "I — Prey", paragraphs("Survive first contact. Find food, shelter, a defensible camp, and enough bearings to return after a bad night.", "You are not expected to conquer every nearby ruin. Learn what to avoid and mark it for later."), -8, 0),
      check("workshop", "II — The First Workshop", paragraphs("Turn water and motion into a visible Create workshop. Processing lines should become part of the settlement, not vanish into a single magic block.", "Andesite machinery is the first dependable form of mastery."), -6, 0, { after: "prey" }),
      check("drifter", "III — The Drifter", paragraphs("Assemble a small Aeronautics craft: closer to a flying tent than a warship.", "Its purpose is range, shelter, and recovery. Every later expedition should leave the craft more capable than before."), -4, 0, { after: "workshop" }),
      check("horizon", "IV — Beyond the Safe Horizon", paragraphs("Use maps, the Atlas, roads, and the airship to push farther. This is where rare structures, spell knowledge, and dangerous biomes become deliberate targets.", "Prepare a route home before claiming a prize."), -2, 0, { after: "drifter" }),
      check("furnace", "V — The Furnace", paragraphs("The Nether is an industrial and military frontier. Secure transport, heat-resistant equipment, and a resupply point before treating it as a resource dimension.", "Its bosses and materials feed both the Eye hunt and advanced craft."), 0, 0, { after: "horizon" }),
      check("silence", "VI — Beneath Silence", paragraphs("Ancient Cities and the Otherside reward patience more than damage. Sound, darkness, and escape routes matter.", "The Stalker guards one of the sixteen Eyes; reaching it should feel like an expedition beneath the known world."), 2, 0, { after: "furnace" }),
      check("opening", "VII — The Opening Source", paragraphs("By now, workshops, spell traditions, and boss hunts intersect. Collect any twelve distinct Eyes and prepare the portal.", "The four skipped Eyes are choices, not failures. Return later if the hunt still calls."), 4, 0, { after: "silence" }),
      check("source", "VIII — The Source", paragraphs("Defeat the Ender Dragon and open access to the End's deeper resources and possibilities.", "This is the opening of the late game, not the credits roll."), 6, 0, { after: "opening" }),
      check("mastery", "IX — Mastery", paragraphs("Complete the systems you chose to own: a fortress, a mature workshop, an heirloom armament, coherent magic, and an expedition flotilla.", "A finished story is measured by what the world now contains because you crossed it."), 8, 0, { after: "source", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "eyes_of_end",
    title: "The Eyes of End",
    group: "start",
    icon: "minecraft:ender_eye",
    order: 2,
    bossRing: true,
    quests: [
      kill("black_eye", "Black Eye — Hullbreaker", paragraphs("Hunt: Hullbreaker.", "Region: the Abyssal Chasm.", "A player kill guarantees the Black Eye. Bring underwater mobility, a retreat route, and a way to recover gear from depth."), "alexscaves:hullbreaker", "endrem:black_eye", 0, -8),
      kill("cold_eye", "Cold Eye — Frostmaw", paragraphs("Hunt: Frostmaw.", "Region: frozen wilderness.", "Its stillness is a warning, not scenery. Read the arena before waking it."), "mowziesmobs:frostmaw", "endrem:cold_eye", 3.1, -7.4),
      kill("corrupted_eye", "Corrupted Eye — Dead King", paragraphs("Hunt: the Dead King.", "Structure: Catacombs.", "Expect a spell duel in confined ground. Carry cover, healing, and an answer to undead pressure."), "irons_spellbooks:dead_king", "endrem:corrupted_eye", 5.7, -5.7),
      kill("cryptic_eye", "Cryptic Eye — Stalker", paragraphs("Hunt: the Stalker.", "Structure: Ancient Temple in the Otherside.", "Silence and escape planning matter more than a reckless damage race."), "deeperdarker:stalker", "endrem:cryptic_eye", 7.4, -3.1),
      kill("cursed_eye", "Cursed Eye — Maledictus", paragraphs("Hunt: Maledictus.", "Structure: Frosted Prison.", "Treat the prison as part of the encounter. Clear a safe approach before committing."), "cataclysm:maledictus", "endrem:cursed_eye", 8, 0),
      kill("evil_eye", "Evil Eye — Gorgon", paragraphs("Hunt: Gorgon.", "Structure: Gorgon Temple.", "Do not meet a petrifying gaze unprepared. The best victory begins before entering the chamber."), "iceandfire:gorgon", "endrem:evil_eye", 7.4, 3.1),
      kill("exotic_eye", "Exotic Eye — Leviathan", paragraphs("Hunt: the Leviathan.", "Structure: Sunken City.", "This is a deep-water siege. Plan air, mobility, ranged pressure, and corpse recovery."), "cataclysm:the_leviathan", "endrem:exotic_eye", 5.7, 5.7),
      kill("guardian_eye", "Guardian Eye — Elder Guardian", paragraphs("Hunt: an Elder Guardian.", "Structure: Ocean Monument.", "Milk handles mining fatigue; it does not replace breathing, navigation, or an exit plan."), "minecraft:elder_guardian", "endrem:guardian_eye", 3.1, 7.4),
      kill("lost_eye", "Lost Eye — Ferrous Wroughtnaut", paragraphs("Hunt: Ferrous Wroughtnaut.", "Structure: Wrought Chamber.", "Armor is not an invitation to trade blows. Observe the pattern and strike the opening."), "mowziesmobs:ferrous_wroughtnaut", "endrem:lost_eye", 0, 8),
      kill("magical_eye", "Magical Eye — Archevoker", paragraphs("Hunt: Archevoker.", "Structure: Evoker Fort.", "Control the room and its summons. Spell resistance and ranged answers earn their weight here."), "irons_spellbooks:archevoker", "endrem:magical_eye", -3.1, 7.4),
      kill("nether_eye", "Nether Eye — Ignis", paragraphs("Hunt: Ignis.", "Structure: Burning Arena.", "Fire protection is only the first layer. Build a resupply route before challenging the arena."), "cataclysm:ignis", "endrem:nether_eye", -5.7, 5.7),
      kill("old_eye", "Old Eye — Ancient Remnant", paragraphs("Hunt: Ancient Remnant.", "Structure: Cursed Pyramid.", "Open ground and heavy impacts punish panic. Prepare mobility and a clean battlefield."), "cataclysm:ancient_remnant", "endrem:old_eye", -7.4, 3.1),
      kill("rogue_eye", "Rogue Eye — Cyclops", paragraphs("Hunt: Cyclops.", "Structure: Cyclops Cave.", "Its cave is both lair and trap. Scout entrances before turning the fight loud."), "iceandfire:cyclops", "endrem:rogue_eye", -8, 0),
      kill("undead_eye", "Undead Eye — Dread Lich", paragraphs("Hunt: Dread Lich.", "Structure: Dread Mausoleum.", "Expect summons and sustained undead pressure. Secure the corridors behind you."), "iceandfire:dread_lich", "endrem:undead_eye", -7.4, -3.1),
      kill("witch_eye", "Witch Eye — Licowitch", paragraphs("Hunt: Licowitch.", "Region: Candy Cavity.", "The bright biome conceals a serious spellcaster. Bring cleansing, cover, and controlled damage."), "alexscaves:licowitch", "endrem:witch_eye", -5.7, -5.7),
      kill("wither_eye", "Wither Eye — The Wither", paragraphs("Hunt: the Wither.", "Arena: your choice.", "Choose the battlefield as carefully as the equipment. Keep the destruction away from infrastructure you intend to keep."), "minecraft:wither", "endrem:wither_eye", -3.1, -7.4),
      kill(
        "dragon",
        "The Ender Dragon — Opening the Source",
        paragraphs(
          "Any twelve completed Eye hunts open this route. All sixteen lines are shown because every Eye is valid; four may be left for later.",
          "Place twelve distinct Eyes in the End portal, enter prepared for a recovery run, and defeat the Dragon.",
          "The Dragon opens the late game. It does not end Creating Stories."
        ),
        "minecraft:ender_dragon",
        "minecraft:dragon_head",
        0,
        0,
        { dependsOnAll: true, minDependencies: 12, shape: "diamond", size: 2.2, minWidth: 340 }
      ),
    ],
  },
  {
    slug: "create",
    title: "Create — The First Workshop",
    group: "engineering",
    icon: "create:mechanical_press",
    order: 0,
    quests: [
      check("principle", "Visible Work", paragraphs("Create is the pack's industrial language. Rotation, belts, fluids, and moving assemblies should make the settlement legible.", "When a machine solves a problem, leave enough of it visible that another traveller can understand the solution."), -7, 0, { icon: "create:goggles" }),
      item("alloy", "Andesite Foundations", paragraphs("Andesite Alloy begins dependable mechanical construction.", "Use JEI for the current recipe. This book marks purpose and sequence, not every ingredient."), "create:andesite_alloy", -5, 0, { after: "principle" }),
      item("power", "First Rotation", paragraphs("A Water Wheel is stable early power. Build room for shafts, gearboxes, and later expansion instead of burying the source in a wall."), "create:water_wheel", -3, 0, { after: "alloy" }),
      item("press", "Mechanical Pressure", paragraphs("The Mechanical Press turns rotational power into repeatable processing. Give it a depot or belt and space to become part of a line."), "create:mechanical_press", -1, -1, { after: "power" }),
      item("mixer", "Controlled Mixtures", paragraphs("A Mechanical Mixer and Basin open alloying and bulk processing.", "Heat, speed, and recipe order are physical constraints. Design around them."), "create:mechanical_mixer", -1, 1, { after: "power" }),
      item("brass", "The Brass Threshold", paragraphs("Brass marks the move from simple motion to controlled logistics.", "Secure the Nether route and build enough capacity that brass mechanisms are infrastructure, not curiosities."), "create:brass_ingot", 2, 0, { after: ["press", "mixer"] }),
      item("precision", "Precision Mechanisms", paragraphs("Precision Mechanisms reward reliable sequencing. A good line can be observed, repaired, and expanded without dismantling the workshop."), "create:precision_mechanism", 4, 0, { after: "brass" }),
      item("crafters", "Machines That Assemble", paragraphs("Mechanical Crafters are a late workshop language for complex shapes.", "Use them where their visible choreography adds meaning; the goal is a workshop with character, not the smallest possible box."), "create:mechanical_crafter", 6, 0, { after: "precision", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "aeronautics",
    title: "Aeronautics — The Flying Home",
    group: "engineering",
    icon: "aeronautics:propeller_bearing",
    order: 1,
    quests: [
      check("principle", "A Ship, Not a Mount", paragraphs("Aeronautics turns Create machinery into a physical expedition vessel. The first craft should be modest: shelter, controls, storage, and a safe place to stand.", "Sable supplies the physics beneath the craft. Aeronautics supplies the pieces you build and operate."), -7, 0, { icon: "aeronautics:aviators_goggles" }),
      item("envelope", "Lift Envelope", paragraphs("A White Envelope is the beginning of deliberate lift.", "Balance mass and layout before decorating. A beautiful wreck is still a wreck."), "aeronautics:white_envelope", -5, 0, { after: "principle" }),
      item("propeller", "Wooden Propeller", paragraphs("The Wooden Propeller is early thrust. Place propulsion where moving parts have clearance and the pilot can inspect them."), "aeronautics:wooden_propeller", -3, 0, { after: "envelope" }),
      item("bearing", "Propeller Bearing", paragraphs("The Propeller Bearing converts Create rotation into useful thrust.", "Treat controls, power transmission, and emergency shutdown as one system."), "aeronautics:propeller_bearing", -1, 0, { after: "propeller" }),
      item("burner", "Adjustable Heat", paragraphs("The Adjustable Burner belongs to the ship's growing engine room.", "Fuel range and flight range should become expedition planning problems, not an invisible timer."), "aeronautics:adjustable_burner", 1, 0, { after: "bearing" }),
      item("gyro", "Gyroscopic Control", paragraphs("A Gyroscopic Propeller Bearing improves control as the vessel becomes heavier and more ambitious.", "Upgrade the frame, access paths, and recovery supplies alongside the controls."), "aeronautics:gyroscopic_propeller_bearing", 3, 0, { after: "burner" }),
      item("smart", "Smart Propulsion", paragraphs("A Smart Propeller belongs on a mature expedition craft.", "By this point the vessel should carry a workshop function, navigation supplies, and a clear role in the flotilla."), "aeronautics:smart_propeller", 5, 0, { after: "gyro" }),
      check("flotilla", "From Drifter to Flotilla", paragraphs("One ship becomes a flying workshop; several ships become doctrine.", "Separate exploration, cargo, rescue, and combat roles when the campaign reaches mastery. The final objective is not merely flight—it is a world made traversable."), 7, 0, { after: "smart", icon: "aeronautics:pearlescent_levitite", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "truly_modular",
    title: "Truly Modular — The Heirloom",
    group: "engineering",
    icon: "miapi:modular_work_bench",
    order: 2,
    quests: [
      item("bench", "The Modular Workbench", paragraphs("The Modular Workbench is where ordinary equipment becomes a persistent creation.", "Parts can be replaced and salvaged through their hierarchy. Read the preview before committing a valuable material."), "miapi:modular_work_bench", -6, 0),
      item("sword", "A Blade With a Future", paragraphs("A Modular Sword is a beginning, not a disposable tier.", "Replace worn or outgrown parts instead of abandoning the identity of the weapon."), "miapi:modular_sword", -4, -1, { after: "bench" }),
      item("pickaxe", "A Working Tool", paragraphs("A Modular Pickaxe carries workshop history into every mine.", "Pack-owned zinc, brass, rose-quartz, and canonical copper materials connect the equipment system to the wider workshop."), "miapi:modular_pickaxe", -4, 1, { after: "bench" }),
      item("bow", "Truly Modular Archery", paragraphs("Modular bows follow the same inheritance: frame, descendants, and materials can be understood and replaced.", "Looted mundane ranged gear may convert; ability-bearing weapons remain protected."), "miapi:modular_bow", -1, -1, { after: ["sword", "pickaxe"] }),
      item("armor", "Truly Modular Armory", paragraphs("Modular armor extends the masterwork idea to protection.", "Automatic conversion is conservative. Mundane material variants may convert; magical, boss, relic, powered, and spellcaster armor keeps its original identity."), "miapi:modular_chestplate", -1, 1, { after: ["sword", "pickaxe"] }),
      check("salvage", "Parts Survive Change", paragraphs("Detached parts and complete equipment retain readable names, including generated materials.", "Salvage is hierarchy-safe: replacing a parent module must not silently erase valuable descendants. Inspect what returns before rebuilding."), 2, 0, { after: ["bow", "armor"], icon: "miapi:modular_work_bench" }),
      check("exceptions", "Exceptions Are Deliberate", paragraphs("Dragonsteel comes from the Dragonforge. Boss weapons remain unique sidegrades. Relics, powered tools, and spellcaster equipment keep the abilities that make them meaningful.", "The modular system owns ordinary craft and long-lived personal equipment; it does not consume every special item in the pack."), 4, 0, { after: "salvage", icon: "iceandfire:dragonsteel_fire_ingot" }),
      check("heirloom", "Name the Masterwork", paragraphs("Choose one modular weapon or tool to carry across the campaign. Repair it, replace its parts, and let its construction record the places you reached.", "Mastery is not the highest material alone. It is a piece of equipment whose history you can explain."), 6, 0, { after: "exceptions", icon: "miapi:modular_sword", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "unified_magic",
    title: "Unified Magic — One Spellbook",
    group: "magic",
    icon: "ars_n_spells:spell_loom",
    order: 0,
    quests: [
      check("principle", "One Casting Practice", paragraphs("Ars Nouveau and Iron's Spells are not parallel careers here. Their knowledge converges into one equipped Iron's spellbook, selected from Iron's native spell wheel, and paid from one shared mana pool.", "The extra Iron's mana bar is hidden intentionally. One visible bar is the source of truth."), -6, 0, { icon: "irons_spellbooks:copper_spell_book" }),
      item("loom", "The Spell Loom", paragraphs("The Spell Loom exports a constructed Ars spell into an Iron-compatible carrier.", "Supply the Ars spell source and the required blank Iron scroll. The resulting carrier is an intermediate, not a normal Iron spell scroll."), "ars_n_spells:spell_loom", -4, 0, { after: "principle" }),
      check("carrier", "Handle the Carrier Safely", paragraphs("Do not put an Ars 'n Spells Loom carrier into Iron's Inscription Table. That invalid route can crash the current Inscription Table screen.", "Carry it to an Ars Ritual Brazier and use the binding ritual described next."), -2, 0, { after: "loom", icon: "irons_spellbooks:scroll", minWidth: 340 }),
      item("binding", "Spellbook Binding", paragraphs("Craft the Spellbook Binding ritual and place the valid Loom-created carrier with a real Iron's spellbook at the Ritual Brazier.", "The ritual binds the Ars payload into that book. This is the supported progression route."), "ars_n_spells:spellbook_binding", 0, 0, { after: "carrier" }),
      check("equip", "The Equipped Book Counts", paragraphs("Equip the bound spellbook in the Curios spellbook slot. Empty hands are valid: the integration resolves the equipped book after checking Iron's recorded casting item and both hands.", "Only the candidate carrying the matching Ars proxy payload is accepted."), 2, 0, { after: "binding", icon: "irons_spellbooks:copper_spell_book" }),
      check("cast", "Wheel, Cast, Confirm", paragraphs("Select the proxy through Iron's native wheel and cast. A successful constructed spell must produce its real Ars effect—not merely a sound or hand animation.", "Right-click casts. Sneak-right-click can cycle where appropriate. Mana must be charged once."), 4, 0, { after: "equip", icon: "ars_nouveau:novice_spell_book" }),
      check("traditions", "Three Traditions, Clear Ownership", paragraphs("Iron's owns active combat schools, found scrolls, and encounter magic. Ars owns theory, utility, and constructed spells. Malum owns spirits, runes, and soul behavior.", "Ars automation that replaces Create's workshop role is outside this pack's intended path."), 6, 0, { after: "cast", icon: "malum:encyclopedia_arcana", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "irons_spells",
    title: "Iron's Spells — Combat Language",
    group: "magic",
    icon: "irons_spellbooks:copper_spell_book",
    order: 1,
    quests: [
      item("book", "A Copper Spellbook", paragraphs("Iron's spellbooks are the physical home of the unified casting practice.", "Begin with capacity you can afford. A spellbook is equipment, not a one-time crafting gate."), "irons_spellbooks:copper_spell_book", -6, 0),
      item("scroll", "Knowledge in the Wild", paragraphs("Scrolls turn exploration into magical growth. Towers, forts, catacombs, and dangerous libraries matter because knowledge has a place in the world.", "Keep useful duplicates; the Inscription Table and Arcane Anvil give them later purpose."), "irons_spellbooks:scroll", -4, 0, { after: "book" }),
      item("table", "The Inscription Table", paragraphs("The Inscription Table manages native Iron's spells and scroll inscription.", "Loom carriers from the unified integration are not valid inputs. Bind those at the Ars Ritual Brazier instead."), "irons_spellbooks:inscription_table", -2, -1, { after: "scroll" }),
      item("anvil", "The Arcane Anvil", paragraphs("The Arcane Anvil supports the maintenance and advancement of magical equipment.", "Treat combat magic as a loadout: range, control, escape, and damage all compete for limited slots."), "irons_spellbooks:arcane_anvil", -2, 1, { after: "scroll" }),
      item("gold", "A Larger Repertoire", paragraphs("A Gold Spellbook expands the practical spell loadout. Upgrade when the new capacity changes how you prepare, not merely because a tier exists."), "irons_spellbooks:gold_spell_book", 1, 0, { after: ["table", "anvil"] }),
      item("diamond", "Expedition-Grade Casting", paragraphs("A Diamond Spellbook belongs in difficult structure and boss expeditions.", "Test native Iron's spells and bound Ars proxies after every major equipment change."), "irons_spellbooks:diamond_spell_book", 3, 0, { after: "gold" }),
      item("dragonskin", "Draconic Binding", paragraphs("The Dragonskin Spellbook joins exploration, dragon hunting, and magical mastery.", "It is a late campaign tool, not the end of practice. Schools, spell choice, and preparation still decide encounters."), "irons_spellbooks:dragonskin_spell_book", 5, 0, { after: "diamond", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "ars_nouveau",
    title: "Ars Nouveau — Theory & Utility",
    group: "magic",
    icon: "ars_nouveau:novice_spell_book",
    order: 2,
    quests: [
      item("notebook", "Read the Worn Notebook", paragraphs("The Worn Notebook is the primary Ars reference. Use it beside this campaign guide for glyph details and apparatus mechanics.", "In Creating Stories, Ars is theory, utility, and constructed spellcraft—not a replacement factory for Create."), "ars_nouveau:worn_notebook", -7, 0),
      item("book", "A Novice Vocabulary", paragraphs("The Novice Spell Book teaches how glyphs become a sentence.", "Start with reliable movement, light, breaking, and controlled combat effects. A useful spell is one you can afford to cast repeatedly."), "ars_nouveau:novice_spell_book", -5, 0, { after: "notebook" }),
      item("gem", "Condensed Source", paragraphs("Source Gems are the portable material language of Ars.", "Build renewable, visible support for the magic you actually use. Keep heavy industrial work in the Create workshop."), "ars_nouveau:source_gem", -3, 0, { after: "book" }),
      item("imbuement", "The Imbuement Chamber", paragraphs("The Imbuement Chamber is an early bridge from resources to Source craft.", "Give magical apparatus a deliberate study space rather than scattering it between unrelated machines."), "ars_nouveau:imbuement_chamber", -1, -1, { after: "gem" }),
      item("scribe", "The Scribe's Table", paragraphs("The Scribe's Table expands the glyph vocabulary.", "New glyphs are capability milestones. Test them in small spells before binding complicated constructions into the unified book."), "ars_nouveau:scribes_table", -1, 1, { after: "gem" }),
      item("apparatus", "The Enchanting Apparatus", paragraphs("The Enchanting Apparatus makes recipes spatial and readable.", "Pedestals, reagents, and the central apparatus should form a ritual workspace whose state can be inspected."), "ars_nouveau:enchanting_apparatus", 2, -1, { after: ["imbuement", "scribe"] }),
      item("brazier", "The Ritual Brazier", paragraphs("The Ritual Brazier owns one-shot rituals, including the pack's Spellbook Binding route.", "For a valid Loom carrier and Iron spellbook, allow the ritual to finish and bind the payload before removing anything."), "ars_nouveau:ritual_brazier", 2, 1, { after: ["imbuement", "scribe"] }),
      item("archmage", "An Archmage's Grammar", paragraphs("The Archmage Spell Book marks deep Ars knowledge, but the campaign's final casting practice still converges on the equipped Iron's book.", "Mastery means understanding both the constructed spell and the vessel that carries it."), "ars_nouveau:archmage_spell_book", 5, 0, { after: ["apparatus", "brazier"], shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "malum",
    title: "Malum — Spirits & Meaning",
    group: "magic",
    icon: "malum:encyclopedia_arcana",
    order: 3,
    quests: [
      item("book", "Encyclopedia Arcana", paragraphs("Malum begins with its Encyclopedia Arcana. Its progression is symbolic and material: spirits, runewood, soulwood, altars, and runes each carry meaning.", "Read entries as ritual instructions, not decoration."), "malum:encyclopedia_arcana", -7, 0),
      item("altar", "The Spirit Altar", paragraphs("The Spirit Altar establishes a place for spirit-work.", "Keep jars and reagents nearby. A coherent occult workshop makes later recipes easier to understand and recover."), "malum:spirit_altar", -5, 0, { after: "book" }),
      item("crucible", "The Spirit Crucible", paragraphs("The Spirit Crucible refines the material side of soul magic.", "Unlike Ars spell construction or Iron's combat schools, Malum's identity is transformation through spirits and resonance."), "malum:spirit_crucible", -3, 0, { after: "altar" }),
      item("jar", "Contain the Harvest", paragraphs("Spirit Jars turn a fleeting drop into managed magical stock.", "Collect deliberately. Different prey and places produce different colors and uses."), "malum:spirit_jar", -1, -1, { after: "crucible" }),
      item("runewood", "Runewood", paragraphs("Runewood begins Malum's living material progression.", "Plant what you can renew. Essential knowledge and basic magical materials should remain multiplayer-safe."), "malum:runewood_sapling", -1, 1, { after: "crucible" }),
      item("pylon", "Arcana in the Room", paragraphs("The Arcana Pylon extends the ritual workshop beyond a single block.", "Space and arrangement are part of the craft; leave room for the system to grow."), "malum:arcana_pylon", 2, -1, { after: ["jar", "runewood"] }),
      item("catalyzer", "Catalyzed Spirits", paragraphs("The Spirit Catalyzer deepens the workshop's transformations.", "By this stage, spirit supply should be planned like any other expedition resource."), "malum:spirit_catalyzer", 2, 1, { after: ["jar", "runewood"] }),
      item("rune", "Rune of Spell Mastery", paragraphs("Runes turn Malum's spirit economy into persistent capability.", "This rune represents the point where soul-work can meaningfully support the unified caster without losing its own identity."), "malum:rune_of_spell_mastery", 5, 0, { after: ["pylon", "catalyzer"], shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "ice_and_fire",
    title: "Ice and Fire — Draconic Craft",
    group: "world",
    icon: "iceandfire:bestiary",
    order: 0,
    quests: [
      item("bestiary", "The Bestiary", paragraphs("The Bestiary is your field authority on dragons, gorgons, cyclopes, dread creatures, and their materials.", "Study before hunting. Most Ice and Fire encounters punish ignorance more sharply than low damage."), "iceandfire:bestiary", -6, 0),
      item("bone", "Dragonbone", paragraphs("Dragonbone turns a successful hunt into durable craft.", "Ordinary copper tools may enter the modular ecosystem; draconic materials and ability-bearing gear keep their special routes."), "iceandfire:dragonbone", -4, 0, { after: "bestiary" }),
      item("skull", "Proof of Scale", paragraphs("A dragon skull records the age and element of a defeated dragon.", "The journey from scavenged remains to deliberate dragon hunts should be visible in your equipment and trophy hall."), "iceandfire:dragon_skull_fire", -2, 0, { after: "bone" }),
      item("forge", "The Dragonforge", paragraphs("Dragonsteel is made only through the Dragonforge. This preserves the scale, danger, and infrastructure of draconic metallurgy.", "Build the forge as a major facility. Fire, ice, and lightning each demand their own understanding."), "iceandfire:dragonforge_fire_core", 0, 0, { after: "skull" }),
      item("steel", "Dragonsteel", paragraphs("Dragonsteel is a late expedition material and a valid expression of mastery.", "It does not erase boss weapons or relics. Those remain unique sidegrades with their own reasons to exist."), "iceandfire:dragonsteel_fire_ingot", 2, 0, { after: "forge" }),
      check("eyes", "Gorgon, Cyclops, Dread Lich", paragraphs("Three Ice and Fire hunts carry Eyes: Gorgon for the Evil Eye, Cyclops for the Rogue Eye, and Dread Lich for the Undead Eye.", "Their structures and counterplay differ. Consult the Eye chapter before choosing which twelve hunts fit your expedition."), 4, 0, { after: "steel", icon: "endrem:evil_eye" }),
      check("mastery", "A Draconic Facility", paragraphs("Complete a defended Dragonforge facility, secure renewable support materials, and keep specialized equipment for different dragon elements.", "The achievement is not merely an ingot. It is the infrastructure and doctrine required to make another safely."), 6, 0, { after: "eyes", icon: "iceandfire:dragonsteel_lightning_ingot", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "cataclysm",
    title: "Cataclysm — Apex Ruins",
    group: "world",
    icon: "cataclysm:mechanical_fusion_anvil",
    order: 1,
    quests: [
      check("principle", "Structures Are Arenas", paragraphs("Cataclysm bosses are tied to monumental ruins. The approach, arena, and retreat path are part of each fight.", "Do not drag an apex encounter into infrastructure you cannot afford to lose."), -6, 0, { icon: "cataclysm:monstrous_horn" }),
      item("fire", "Altar of Fire", paragraphs("The Altar of Fire points toward Ignis and the Burning Arena.", "Prepare fire resistance, sustained healing, and a Nether resupply route before committing."), "cataclysm:altar_of_fire", -4, -1, { after: "principle" }),
      item("abyss", "Altar of Abyss", paragraphs("The Altar of Abyss belongs to the Leviathan's path.", "Underwater boss preparation must include air, three-dimensional mobility, ranged pressure, and gear recovery."), "cataclysm:altar_of_abyss", -4, 1, { after: "principle" }),
      item("void", "Altar of Void", paragraphs("The Altar of Void marks another branch of Cataclysm's apex progression.", "Even when a boss is not required for an Eye, its arena and unique equipment remain meaningful side objectives."), "cataclysm:altar_of_void", -1, -1, { after: ["fire", "abyss"] }),
      item("amethyst", "Altar of Amethyst", paragraphs("The Altar of Amethyst extends the ruin network into another distinct encounter language.", "Treat every altar as a warning to finish preparation before activation."), "cataclysm:altar_of_amethyst", -1, 1, { after: ["fire", "abyss"] }),
      item("anvil", "Mechanical Fusion Anvil", paragraphs("The Mechanical Fusion Anvil turns apex spoils into deliberate equipment choices.", "Boss gear remains protected from ordinary automatic modular conversion so its abilities and identity survive."), "cataclysm:mechanical_fusion_anvil", 2, 0, { after: ["void", "amethyst"] }),
      check("eyes", "Four Cataclysm Eyes", paragraphs("Maledictus yields the Cursed Eye, Leviathan the Exotic Eye, Ignis the Nether Eye, and Ancient Remnant the Old Eye.", "You need only twelve of all sixteen Eyes. Choose these fights when your route and equipment make sense."), 4, 0, { after: "anvil", icon: "endrem:cursed_eye" }),
      check("mastery", "Apex Sidegrades", paragraphs("Keep boss weapons as specialized answers rather than feeding them into a universal tier ladder.", "A mature arsenal contains tools with different stories and purposes."), 6, 0, { after: "eyes", icon: "cataclysm:mechanical_fusion_anvil", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "mowzies_mobs",
    title: "Mowzie's Mobs — Read the Fight",
    group: "world",
    icon: "mowziesmobs:wrought_axe",
    order: 2,
    quests: [
      check("principle", "Pattern Before Power", paragraphs("Mowzie's encounters communicate openings through animation and position.", "Observe first. A modular masterwork helps, but it cannot replace reading the fight."), -5, 0, { icon: "mowziesmobs:wrought_helmet" }),
      kill("wroughtnaut", "Ferrous Wroughtnaut", paragraphs("Find the Wrought Chamber and face the Ferrous Wroughtnaut.", "Its armor has an answer. Discover the opening instead of treating the encounter as a damage wall."), "mowziesmobs:ferrous_wroughtnaut", "endrem:lost_eye", -3, -1, { after: "principle" }),
      item("axe", "Wrought Axe", paragraphs("The Wrought Axe is a boss sidegrade with its own identity.", "It remains protected from ordinary modular conversion. Keep it for the role it performs."), "mowziesmobs:wrought_axe", -1, -1, { after: "wroughtnaut" }),
      kill("frostmaw", "Frostmaw", paragraphs("Find a sleeping Frostmaw in frozen country.", "Prepare the arena and retreat before waking it. Victory yields progress toward the Cold Eye route."), "mowziesmobs:frostmaw", "endrem:cold_eye", -3, 1, { after: "principle" }),
      item("crystal", "Ice Crystal", paragraphs("The Ice Crystal is a specialized trophy and tool, not merely another material tier.", "Ability-bearing equipment keeps its native behavior in Creating Stories."), "mowziesmobs:ice_crystal", -1, 1, { after: "frostmaw" }),
      check("mastery", "Two Eyes, Two Lessons", paragraphs("The Lost Eye and Cold Eye point to different combat lessons: patient openings and environmental preparation.", "Complete either, both, or leave one among the four hunts you skip before the Dragon."), 2, 0, { after: ["axe", "crystal"], icon: "endrem:lost_eye", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "alexs_caves",
    title: "Alex's Caves — Lost Biomes",
    group: "world",
    icon: "alexscaves:cave_book",
    order: 3,
    quests: [
      item("tablet", "Cave Tablet", paragraphs("Cave Tablets begin the search for Alex's Caves biomes.", "Treat each biome as an expedition region with its own hazards, resources, and extraction problem."), "alexscaves:cave_tablet", -6, 0),
      item("book", "Cave Compendium", paragraphs("The Cave Book records what the biomes teach.", "Use it as the mechanical reference and this chapter as the campaign map."), "alexscaves:cave_book", -4, 0, { after: "tablet" }),
      item("heart", "Heart of Iron", paragraphs("The Heart of Iron belongs to the industrial strangeness of the Magnetic Caves.", "Rare biome materials should become reasons to return, not one-time checklist trophies."), "alexscaves:heart_of_iron", -2, -1, { after: "book" }),
      item("altar", "Abyssal Altar", paragraphs("The Abyssal Altar anchors progression in the dangerous oceanic depths.", "Build underwater mobility and recovery capacity before carrying valuable equipment into the Chasm."), "alexscaves:abyssal_altar", -2, 1, { after: "book" }),
      kill("hullbreaker", "Hullbreaker", paragraphs("The Hullbreaker guards the Black Eye route in the Abyssal Chasm.", "Deep water makes corpse recovery and escape part of the encounter. Prepare both before the kill."), "alexscaves:hullbreaker", "endrem:black_eye", 1, 1, { after: "altar" }),
      kill("licowitch", "Licowitch", paragraphs("The Licowitch guards the Witch Eye route in the Candy Cavity.", "Brightness is camouflage. Prepare for a magical duel with cover and cleansing."), "alexscaves:licowitch", "endrem:witch_eye", 1, -1, { after: "heart" }),
      check("mastery", "Five Biomes, Repeat Expeditions", paragraphs("Do not strip each cave and abandon it. Mark safe access, preserve routes, and return when later systems reveal new uses.", "The Atlas records your travel; it does not reveal which darkness contains the next biome."), 4, 0, { after: ["hullbreaker", "licowitch"], icon: ANTIQUE_ATLAS, shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "deeper_darker",
    title: "Deeper and Darker — Beneath Silence",
    group: "world",
    icon: "deeperdarker:heart_of_the_deep",
    order: 4,
    quests: [
      check("principle", "The City Is Listening", paragraphs("Ancient Cities reward controlled movement, wool, distraction, and a known exit.", "Treat the Warden as a disaster to manage, not a routine loot pinata."), -6, 0, { icon: "minecraft:sculk_sensor" }),
      item("heart", "Heart of the Deep", paragraphs("The Heart of the Deep is the key material in the path beyond the Ancient City.", "Secure it without turning every visit into an unrecoverable equipment loss."), "deeperdarker:heart_of_the_deep", -4, 0, { after: "principle" }),
      item("bone", "Sculk Bone", paragraphs("Sculk Bone carries the material language of the deep.", "Keep samples and learn their recipes before spending everything on a single attempt."), "deeperdarker:sculk_bone", -2, -1, { after: "heart" }),
      item("crystal", "Soul Crystal", paragraphs("Soul Crystals connect the biome's danger to later craft.", "The pack's knowledge routes are intended to remain renewable and multiplayer-safe; preserve shared access."), "deeperdarker:soul_crystal", -2, 1, { after: "heart" }),
      check("portal", "The Otherside Portal", paragraphs("Restore the Ancient City's portal when you can support an expedition through it.", "Mark the route, establish a recovery cache, and assume the first crossing is reconnaissance."), 1, 0, { after: ["bone", "crystal"], icon: "deeperdarker:heart_of_the_deep" }),
      kill("stalker", "The Stalker", paragraphs("Find the Ancient Temple in the Otherside and defeat the Stalker.", "The kill guarantees the Cryptic Eye when credited to a player."), "deeperdarker:stalker", "endrem:cryptic_eye", 3, 0, { after: "portal" }),
      check("mastery", "Return From the Otherside", paragraphs("A successful expedition returns with knowledge, an Eye, and a route another player can follow.", "The real milestone is making the unknown reachable twice."), 5, 0, { after: "stalker", icon: "endrem:cryptic_eye", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "navigation_archaeology",
    title: "Navigation & Archaeology",
    group: "knowledge",
    icon: "betterarcheology:archeology_table",
    order: 0,
    quests: [
      item("atlas", "The Antique Atlas", paragraphs("The Atlas records explored terrain and your own markers. It does not reveal an unexplored world.", "Mark boss arenas, recovery caches, ports, Ancient Cities, dragon territories, and safe airship moorings."), ANTIQUE_ATLAS, -7, 0),
      check("maps", "Maps Before Guesswork", paragraphs("Essential targets should have a discoverable route: cartographer maps, structure loot maps, or information found during exploration.", "Use the world as evidence. Blind required searching is not the intended difficulty."), -5, 0, { after: "atlas", icon: "minecraft:filled_map" }),
      item("compass", "Explorer's Compass — The Fallback", paragraphs("The Explorer's Compass is an expensive post-Wither fallback when normal map routes fail.", "It is not the first answer and should not erase exploration; it prevents essential progression from becoming an endless seed lottery."), "explorerscompass:explorerscompass", -3, 0, { after: "maps" }),
      item("brush", "A Better Brush", paragraphs("Archaeology expands the meaning of structures beyond chests. Bring a brush when ruins, dig sites, or suspicious blocks suggest a slower search.", "Brush routes are tracked in Just Enough Archaeology, including pack integrations."), "betterarcheology:iron_brush", -1, -1, { after: "maps" }),
      item("table", "The Archaeology Table", paragraphs("The Archaeology Table turns recovered fragments into readable progress.", "Keep a field chest for shards and unidentified finds rather than discarding incomplete sets."), "betterarcheology:archeology_table", 1, -1, { after: "brush" }),
      item("artifact", "Unidentified Artifact", paragraphs("An Unidentified Artifact is evidence awaiting interpretation.", "Integrated Dungeons and Structures and Integrated Villages contribute additional brush-table mappings; dig-site fossiliferous dirt is a deliberate route."), "betterarcheology:unidentified_artifact", 3, -1, { after: "table" }),
      check("shared", "Leave a Route for the Next Traveller", paragraphs("On a server, mark entrances and avoid destroying unique access points. Essential knowledge is designed to be renewable, but good expedition practice still matters.", "A map annotation can be as valuable as the loot you carried home."), 5, 0, { after: ["compass", "artifact"], icon: ANTIQUE_ATLAS, shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "ancient_knowledge",
    title: "Ancient Knowledge",
    group: "knowledge",
    icon: "immersiveenchanting:ancient_book",
    order: 1,
    quests: [
      check("principle", "Knowledge Has a Place", paragraphs("Ancient Books and powerful enchantments are tied to themed structures and archaeology rather than one generic chest table.", "Where you search should matter as much as how long you search."), -5, 0, { icon: "minecraft:enchanting_table" }),
      item("book", "Ancient Book", paragraphs("An Ancient Book is a piece of recoverable knowledge.", "Just Enough Archaeology displays supported brushing routes, while structure loot keeps the discovery tied to appropriate places."), "immersiveenchanting:ancient_book", -3, 0, { after: "principle" }),
      check("renewable", "A Renewable Archive", paragraphs("Ancient knowledge is not allowed to become a finite single-player monopoly.", "Create Enchantment Industry provides the printer route for renewable copies once the workshop is advanced enough."), -1, 0, { after: "book", icon: "create:mechanical_crafter" }),
      check("cache", "Safe Across Worlds", paragraphs("The archaeology integration clears generated recipe displays when changing worlds in one client session.", "This prevents stale registry-backed Ancient Books from breaking recipe synchronization. If a recipe viewer looks wrong after a world change, report it rather than rebuilding the pack data by hand."), 1, 0, { after: "renewable", icon: "betterarcheology:archeology_table", minWidth: 340 }),
      check("library", "Build the Expedition Library", paragraphs("Store spare books, maps, tablets, Bestiaries, notebooks, and field records together.", "A library turns scattered discoveries into shared infrastructure and makes late-game preparation faster for every traveller."), 3, 0, { after: "cache", icon: "minecraft:chiseled_bookshelf" }),
      check("mastery", "Knowledge That Survives the Expedition", paragraphs("A mature archive has a source, a renewable route where required, and enough documentation that another player can repeat the discovery.", "That is the pack's standard for progression-safe knowledge."), 5, 0, { after: "library", icon: "immersiveenchanting:ancient_book", shape: "diamond", size: 1.5 }),
    ],
  },
  {
    slug: "relics_artifacts",
    title: "Relics & Artifacts",
    group: "knowledge",
    icon: "relics:reflective_necklace",
    order: 2,
    quests: [
      check("principle", "Themed Finds First", paragraphs("Relics and Artifacts are exploration sidegrades. Their meaning comes from finding them in places that fit their theme, not from a universal reward bag.", "Ability-bearing relics remain outside ordinary modular conversion."), -4, 0, { icon: "relics:reflective_necklace" }),
      check("curator", "How the Curator Rolls", paragraphs("A matching themed Relics entry receives its normal twenty-percent attempt.", "Only when that produces nothing does the curator roll a two-percent Overworld fallback pool. A low item weight alone would not create this behavior, so the pack owns the selection logic."), -2, 0, { after: "principle", icon: "minecraft:chest" }),
      check("no_duplicates", "No Double Reward", paragraphs("The curator does not add a fallback when generated loot already contains a configured artifact or any Relics item.", "The native Relics loot modifier is intentionally disabled while the curator is installed."), 0, 0, { after: "curator", icon: "minecraft:barrier" }),
      check("viewer", "JEI Shows the Real Route", paragraphs("Relic Loot Viewer reads the curator's actual data and shows whether an item has a themed route or only the lucky fallback.", "Use that display before farming a structure whose theme does not match the relic you want."), 2, 0, { after: "no_duplicates", icon: "minecraft:spyglass" }),
      check("collection", "A Cabinet of Sidegrades", paragraphs("Keep relics as a collection of specialized expedition tools. Choose them for a route, boss, or recovery problem.", "Mastery is knowing which strange object belongs in the satchel today."), 4, 0, { after: "viewer", icon: "relics:reflective_necklace", shape: "diamond", size: 1.5 }),
    ],
  },
];

const allQuestIds = new Map();
for (const chapter of chapters) {
  chapter.id = id(`chapter:${chapter.slug}`);
  chapter.groupId = id(`group:${chapter.group}`);
  for (const quest of chapter.quests) {
    quest.id = id(`quest:${chapter.slug}:${quest.slug}`);
    quest.task.id = id(`task:${chapter.slug}:${quest.slug}`);
    if (quest.reward) {
      quest.rewardId = id(`reward:${chapter.slug}:${quest.rewardKey ?? quest.slug}`);
    }
    if (allQuestIds.has(quest.id)) {
      throw new Error(`Quest ID collision: ${quest.id}`);
    }
    allQuestIds.set(quest.id, `${chapter.slug}/${quest.slug}`);
  }
}

function questBySlug(chapter, slug) {
  const quest = chapter.quests.find((entry) => entry.slug === slug);
  if (!quest) {
    throw new Error(`Unknown dependency ${chapter.slug}/${slug}`);
  }
  return quest;
}

function dependencyIds(chapter, quest) {
  if (quest.dependsOnAll) {
    return chapter.quests.filter((entry) => entry !== quest).map((entry) => entry.id);
  }
  if (!quest.after) {
    return [];
  }
  const slugs = Array.isArray(quest.after) ? quest.after : [quest.after];
  return slugs.map((slug) => questBySlug(chapter, slug).id);
}

function stackData(stack) {
  return typeof stack === "string" ? { id: stack } : stack;
}

function renderComponents(components) {
  const entries = Object.entries(components ?? {});
  if (entries.length === 0) {
    return "";
  }
  return `components: { ${entries.map(([key, value]) => `${quote(key)}: ${quote(value)}`).join(", ")} }, `;
}

function renderStack(stack, includeCount) {
  const data = stackData(stack);
  const count = includeCount ? "count: 1, " : "";
  return `{ ${renderComponents(data.components)}${count}id: ${quote(data.id)} }`;
}

function renderTask(task) {
  if (task.type === "checkmark") {
    return [
      "{",
      `  id: ${quote(task.id)}`,
      `  type: "checkmark"`,
      "}",
    ].join("\n");
  }
  if (task.type === "item") {
    return [
      "{",
      `  id: ${quote(task.id)}`,
      `  item: ${renderStack(task.item, true)}`,
      `  type: "item"`,
      "}",
    ].join("\n");
  }
  if (task.type === "kill") {
    return [
      "{",
      `  entity: ${quote(task.entity)}`,
      `  id: ${quote(task.id)}`,
      `  type: "kill"`,
      `  value: 1L`,
      "}",
    ].join("\n");
  }
  throw new Error(`Unsupported task type: ${task.type}`);
}

function indent(text, spaces) {
  const prefix = " ".repeat(spaces);
  return text.split("\n").map((line) => `${prefix}${line}`).join("\n");
}

function renderQuest(chapter, quest) {
  const dependencies = dependencyIds(chapter, quest);
  const lines = ["{"];
  if (dependencies.length > 0) {
    lines.push(`  dependencies: [${dependencies.map(quote).join(" ")}]`);
  }
  lines.push(`  icon: ${renderStack(quest.icon ?? chapter.icon, false)}`);
  lines.push(`  id: ${quote(quest.id)}`);
  if (quest.minDependencies) {
    lines.push(`  min_required_dependencies: ${quest.minDependencies}`);
  }
  if (quest.minWidth) {
    lines.push(`  min_width: ${quest.minWidth}`);
  }
  if (quest.reward) {
    lines.push("  rewards: [{");
    lines.push(`    count: 1`);
    lines.push(`    id: ${quote(quest.rewardId)}`);
    lines.push(`    item: ${renderStack(quest.reward, true)}`);
    lines.push(`    team_reward: false`);
    lines.push(`    type: "item"`);
    lines.push("  }]");
  }
  if (quest.shape) {
    lines.push(`  shape: ${quote(quest.shape)}`);
  }
  if (quest.size) {
    lines.push(`  size: ${quest.size.toFixed(1)}d`);
  }
  lines.push("  tasks: [");
  lines.push(indent(renderTask(quest.task), 4));
  lines.push("  ]");
  lines.push(`  x: ${Number(quest.x).toFixed(1)}d`);
  lines.push(`  y: ${Number(quest.y).toFixed(1)}d`);
  lines.push("}");
  return lines.join("\n");
}

function renderChapter(chapter) {
  return [
    "{",
    "  default_hide_dependency_lines: false",
    `  filename: ${quote(chapter.slug)}`,
    `  group: ${quote(chapter.groupId)}`,
    `  icon: ${renderStack(chapter.icon, false)}`,
    `  id: ${quote(chapter.id)}`,
    `  order_index: ${chapter.order}`,
    `  progression_mode: "flexible"`,
    "  quest_links: []",
    "  quests: [",
    chapter.quests.map((quest) => indent(renderQuest(chapter, quest), 4)).join("\n"),
    "  ]",
    "}",
    "",
  ].join("\n");
}

function chapterLocaleEntries(chapter) {
  const entries = [];
  for (const quest of chapter.quests) {
    entries.push(`  quest.${quest.id}.quest_desc: [${quoteRichText(quest.description)}]`);
    entries.push(`  quest.${quest.id}.title: ${quoteRichText(quest.title)}`);
    if (quest.task.type === "checkmark") {
      entries.push(`  task.${quest.task.id}.title: ${quoteRichText("Mark as read")}`);
    }
  }
  return entries;
}

const chapterGroups = [
  "{",
  "  chapter_groups: [",
  ...groups.map((group) => `    { id: ${quote(id(`group:${group.slug}`))} }`),
  "  ]",
  "}",
  "",
].join("\n");

const localeEntries = [
  `  file.0000000000000001.title: ${quoteRichText("Creating Stories — Expedition Journal")}`,
  ...chapters.map((chapter) => `  chapter.${chapter.id}.title: ${quoteRichText(chapter.title)}`),
  ...groups.map((group) => `  chapter_group.${id(`group:${group.slug}`)}.title: ${quoteRichText(group.title)}`),
  ...chapters.flatMap(chapterLocaleEntries),
];

const englishLocale = ["{", ...localeEntries.sort(), "}", ""].join("\n");

const data = [
  "{",
  `  default_autoclaim_rewards: "disabled"`,
  `  default_consume_items: false`,
  `  default_quest_disable_jei: false`,
  `  default_quest_shape: "circle"`,
  `  default_reward_team: false`,
  `  detection_delay: 20`,
  `  disable_gui: false`,
  `  drop_loot_crates: false`,
  `  emergency_items_cooldown: 300`,
  `  fallback_locale: "en_us"`,
  `  grid_scale: 0.5d`,
  `  icon: ${renderStack(ANTIQUE_ATLAS, false)}`,
  `  lock_message: "Complete the connected field notes first."`,
  `  pause_game: false`,
  `  progression_mode: "flexible"`,
  `  show_lock_icons: true`,
  `  version: 13`,
  "}",
  "",
].join("\n");

const files = new Map([
  ["data.snbt", data],
  ["chapter_groups.snbt", chapterGroups],
  ["lang/en_us.snbt", englishLocale],
]);

for (const chapter of chapters) {
  files.set(`chapters/${chapter.slug}.snbt`, renderChapter(chapter));
}

for (const root of roots) {
  const absoluteRoot = resolve(root);
  const obsoleteSplitLocale = resolve(root, "lang", "en_us");
  if (!obsoleteSplitLocale.startsWith(`${absoluteRoot}${sep}`)) {
    throw new Error(`Refusing to remove path outside generated quest root: ${obsoleteSplitLocale}`);
  }
  rmSync(obsoleteSplitLocale, { recursive: true, force: true });
  for (const [relative, content] of files) {
    const destination = join(root, relative);
    mkdirSync(join(destination, ".."), { recursive: true });
    writeFileSync(destination, content, "utf8");
  }
}

const rewardQuests = chapters.flatMap((chapter) => chapter.quests).filter((quest) => quest.reward);
if (rewardQuests.length !== 3 || rewardQuests.some((quest) => quest.slug === "dragon")) {
  throw new Error("The alpha book must contain exactly the three Welcome starter rewards.");
}

const bossChapter = chapters.find((chapter) => chapter.bossRing);
const dragon = bossChapter.quests.find((quest) => quest.slug === "dragon");
if (bossChapter.quests.length !== 17 || dependencyIds(bossChapter, dragon).length !== 16 || dragon.minDependencies !== 12) {
  throw new Error("The Eye chapter must contain sixteen Eye bosses feeding a twelve-of-sixteen Dragon gate.");
}

console.log(`Generated ${chapters.length} chapters and ${chapters.reduce((sum, chapter) => sum + chapter.quests.length, 0)} quests in live and override trees.`);
