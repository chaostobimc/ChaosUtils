# ChaosUtils — 1.21.11 API notes

Everything in this document was verified against the Minecraft 1.21.11 / Fabric API
`0.141.6+1.21.11` sources while the mod was written. It exists so that a future update of
Minecraft (or of the mappings) is a mechanical job instead of a hunt.

## Toolchain

| Part | Value | Why |
| --- | --- | --- |
| Mappings | `loom.officialMojangMappings()` | 1.21.11 renamed the platform classes (`ResourceLocation` → `Identifier`, …); the official mappings are the ones NeoForge/Fabric document for this version. |
| Loom | `1.14.10` (`net.fabricmc.fabric-loom-remap`) | 1.21.11 is the **last obfuscated** Minecraft release; Loom < 1.14 will not build it, and the `-remap` plugin id is the one for obfuscated versions (Loom 1.14 release notes). |
| Gradle / Java | 9.2.1 (wrapper committed) / 21 | Loom 1.14 requires Gradle 9.2+; the wrapper jar in this repo is the official 9.2.1 one (checksum in `docs/BUILD_TROUBLESHOOTING.md`). |
| Loader / API | `0.19.5+` / `0.141.6+1.21.11` | First loader line that supports 1.21.11, matching Fabric API build. |
| Config library | none — ChaosUtils ships its own JSON store and click GUI | Cloth Config for 1.21.11 is a moving target and the mod already needs a custom, animated interface; the built-in store is smaller, dependency-free and rename-safe. |

## Renames that affect this code base

* `ResourceLocation` → `net.minecraft.resources.Identifier` (`Identifier.fromNamespaceAndPath`, `getPath`, `toString`).
* `net.minecraft.{BlockUtil,FileUtil,Util}` → `net.minecraft.util.*`.
* `advancements.critereon` → `criterion`.
* `client.model` and `world.entity` are split into subpackages
  (`.animal`, `.monster`, `.object`, `.player`, `.ambient`, `.effects`, `.projectile`, `.boat`, `.equipment`).
  Entity and model types are imported from their new homes.
* `RenderType` constants moved to `RenderTypes`; custom render pipelines use
  `RenderSetup.builder(RenderPipeline)` → `RenderType.create(...)`. **ChaosUtils does not create
  a single render type**: every overlay is drawn with `GuiGraphics#fill`/text so the entire
  render-pipeline rework cannot break it.
* `RenderSystem#setShaderTexture`/`getShaderTexture`, `setTextureMatrix` and `lineWidth` are gone
  (samplers now come from `RenderSystem.getSamplerCache()`), which is another reason to stay on
  `GuiGraphics`.

## Input model (changed in 1.21.9, still current in 1.21.11)

The event records live in `net.minecraft.client.input`:

* `mouseClicked(MouseButtonEvent click, boolean doubleClick)`
* `mouseReleased(MouseButtonEvent click)`
* `mouseDragged(MouseButtonEvent click, double offsetX, double offsetY)`
* `mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)`
* `keyPressed(KeyEvent input)` / `keyReleased(KeyEvent input)` / `charTyped(CharacterEvent input)`

ChaosUtils deliberately only reads `MouseButtonEvent#button()` from those records; the mouse
position is taken from the last rendered frame (`ChaosScreen`), and keyboard input that is not
text (Escape, Enter, the hotkey capture in the settings) is polled with GLFW through
`util/InputUtil`. That keeps the interface independent of any accessor name inside the event
records, which is where mapping drift usually hurts.

`KeyMapping` mutations in 1.21.9+: hold state is `isPressed()`, the one-shot query is
`wasPressed()`, and the constructor takes a `KeyMapping.Category`
(`KeyMapping.Category.MISC` for ChaosUtils). Keys are registered through
`KeyBindingHelper.registerKeyBinding`.

## Screen events (Fabric API)

`Screens.getButtons(Screen)` is used to add the container-search text field, because
`Screen#addRenderableWidget` is protected and the listener lives outside the screen class.
`ScreenEvents.AFTER_INIT` supplies the screen instance, `Screens.getButtons` supplies the widget
list, `Screen#getFont()` supplies the font.

## HUD API (Fabric API)

Every overlay is registered with `HudElementRegistry.addLast(Identifier, HudElement)`; the
`HudElement#render(GuiGraphics, DeltaTracker)` parameters are inferred (the lambda never names
`DeltaTracker`), so a rename of the tick counter type does not touch this code. Registering as a
vanilla HUD element means F1 (hide HUD), HUD scale and element ordering behave exactly as the
player expects.

## Mixin targets

| Mixin | Target | Notes |
| --- | --- | --- |
| `SoundEngineMixin` | `SoundEngine#play(SoundInstance)` | Observation only, never cancelled. |
| `AbstractContainerScreenMixin` | `AbstractContainerScreen#render(GuiGraphics,int,int,float)` | TAIL: draws the search overlay. |
| `AbstractContainerScreenAccessor` | `leftPos`, `topPos`, `imageWidth`, `imageHeight` | The overlay is drawn from outside the class hierarchy, so an accessor is required. |
| `CameraMixin` | `Camera#setup(BlockGetter,Entity,boolean,boolean,float)` | TAIL: applies the perspective lock rotation. |
| `DebugScreenOverlayMixin` | `DebugScreenOverlay#render(GuiGraphics)` | HEAD cancels vanilla when the compact panel replaces it, TAIL draws the compact panel. |
| `EntityMixin` | `Entity#remove(Entity.RemovalReason)` | HEAD: drops per-entity caches (no leaks). |
| `GameRendererMixin` | `GameRenderer#getFov(Camera,float,boolean)` | RETURN: multiplies the computed FOV by the zoom factor. |
| `GuiMixin` | `Gui#renderCrosshair(GuiGraphics,DeltaTracker)` | HEAD: cancels the vanilla crosshair when the designer draws its own. The handler only declares the `GuiGraphics` argument (Mixin allows trimming trailing parameters), so `DeltaTracker` is never named. |
| `MouseHandlerMixin` | `MouseHandler#onScroll(long,double,double)` | HEAD: feeds the zoom wheel; nothing is cancelled. |
| `ParticleEngineMixin` | `ParticleEngine#createParticle(ParticleOptions,ClientLevel,double×6,RandomSource)` | HEAD: cancels hidden particle types client side. |

All injectors use `require = 0` and every handler is wrapped in a defensive try/catch, and
`core/ApiCompat` prints an `[ ok ] / [ MISS ]` line for each hook a few seconds after startup.
If a future Minecraft release moves one of these methods, the affected feature degrades
silently and the log says exactly which one.

### Signatures verified against the decompiled 1.21.11 sources

Every name below was checked against a Mojang-mapped 1.21.11 source tree
(`version.json` -> `1.21.11_unobfuscated`) and against the NeoForge 1.21.11 branch
and the Fabric API `1.21.11` branch. These are the exact signatures ChaosUtils uses.

| Area | 1.21.11 signature | Used for |
| --- | --- | --- |
| `KeyMapping` | `isDown()`, `consumeClick()`, `Category.MISC`, `KeyMapping(String, InputConstants.Type, int, Category)` | every hotkey |
| `GuiGraphics` | `guiWidth()`, `guiHeight()`, `drawString(Font, String, int, int, int, boolean)`, `drawCenteredString(Font, String, int, int, int)`, `enableScissor(int, int, int, int)`, `disableScissor()`, `renderItem(ItemStack, int, int)`, `renderItemDecorations(Font, ItemStack, int, int)`, `pose()` | all rendering |
| GUI matrix stack | `GuiGraphics#pose()` returns `org.joml.Matrix3x2fStack` (2D!), so transforms use `pushMatrix()`/`popMatrix()` + `translate(float, float)`/`scale(float, float)` - **not** `pushPose()`/`popPose()` (that is the 3D `PoseStack`) | animating the click GUI and the radial ring |
| `InputConstants` | `isKeyDown(Window, int)` (takes the window object, not the GLFW handle), `Key#getCreate`/`getOrCreate`, `Key#getDisplayName()` returns **`Component`** | key name labels, polled key state |
| Input model (1.21.9+) | `MouseButtonEvent`, `KeyEvent`, `CharacterEvent`; `mouseClicked(MouseButtonEvent, boolean)`, `mouseReleased(MouseButtonEvent)`, `mouseDragged(MouseButtonEvent, double, double)`, `mouseScrolled(double, double, double, double)` | every ChaosUtils screen |
| `ResourceKey` | `identifier()`, `registry()` - **not** `location()` | dimension names, enchantment ids |
| `SoundInstance` | `getIdentifier()` - **not** `getLocation()`; `getSource()`, `getVolume()`, `getPitch()`, `getX()/getY()/getZ()`, `isRelative()` | sound radar, subtitles |
| `SoundEngine` | `play(SoundInstance)` returns `SoundEngine.PlayResult`; `resume()` | sound hook, volume ducker |
| `SoundManager` | `pauseAllExcept(SoundSource...)`, `resume()` | unfocused volume reducer |
| `SimpleSoundInstance` | `forUI(SoundEvent, float, float)` | UI feedback sounds |
| `Camera` | `xRot()`, `yRot()`, `yaw()`, `position()`, `blockPosition()`, `setup(Level, Entity, boolean, boolean, float)`; `setRotation(float, float)` is `protected` -> reached through the `CameraAccessor` `@Invoker` | perspective lock, projection |
| `GameRenderer` | `getFov(Camera, float, boolean)` returns **`float`** (was `double`) | smooth zoom mixin |
| `DebugScreenOverlay` | `render(GuiGraphics)` | compact F3 overlay |
| `Gui` | `renderCrosshair(GuiGraphics, DeltaTracker)`, `getChat()` | crosshair designer, chat history |
| `Window` | `handle()` - **not** `getWindow()`; `getGuiScaledWidth()/Height()` | GLFW polling in `InputUtil` |
| `Minecraft` | `getDebugOverlay()`, `getFps()`, `getUser()`, `isWindowActive()`, `getWindow().handle()` | debug overlay, ping/TPS |
| `ItemStack` | `get(DataComponentType<T>)` via `DataComponentHolder`, `getEnchantments()`, `getMaxDamage()`, `getDamageValue()`, `isDamageableItem()`, `getHoverName()` | tooltips, durability, counter |
| `ItemContainerContents` / `BundleContents` | `nonEmptyItems()`, `nonEmptyStream()` / `items()` | container + bundle preview |
| `ItemEnchantments` | `keySet()`, `getLevel(Holder<Enchantment>)` | enchantment short names |
| `EditBox` | `EditBox(Font, int, int, int, int, Component)` | container search, text dialogs |
| `Options` | `showSubtitles()`, `getCameraType()/setCameraType()`, `fov()/gamma()/sensitivity()`, `getSoundSourceOptionInstance(SoundSource)` | subtitles, gamma, zoom, ducking |
| `Registry` | `getKey(Object)` -> `Identifier` | item/sound/particle ids |
| `LevelAccessor` | default `getGameTime()`; `Level#getDayTime()` | TPS estimator, debug overlay |

Fabric API parts (branch `1.21.11`, version `0.141.6+1.21.11`):

* `HudElementRegistry.addLast(Identifier, HudElement)` with `HudElement#render(GuiGraphics, DeltaTracker)`
* `ItemTooltipCallback#getTooltip(ItemStack, Item.TooltipContext, TooltipFlag, List<Component>)`
* `KeyBindingHelper.registerKeyBinding(KeyMapping)`
* `Screens.getButtons(Screen)` -> mutable `List<AbstractWidget>`
* `ScreenEvents.AFTER_INIT / remove / beforeRender / afterRender / beforeTick / afterTick`
* `ClientReceiveMessageEvents.CHAT / GAME / ALLOW_CHAT / ALLOW_GAME / CHAT_CANCELED / GAME_CANCELED`
* `ClientTickEvents.START_CLIENT_TICK / END_CLIENT_TICK`

Everything else risky is wrapped in `try/catch` and guarded by `ApiCompat.seen(...)`, so a future
rename degrades a single feature instead of crashing the game.

## Fair play statement

Every feature follows the same three rules:

1. **Read only.** Overlays read the state vanilla already received (entity data, chat, sound
   events, inventory contents). No feature asks the server for anything.
2. **One action, from the player.** The radial menu executes exactly one entry when the player
   releases the key. Nothing is scheduled, repeated, automated or triggered while the player is
   away from the keyboard.
3. **Client side only.** Hiding particles, dimming slots, colouring text, changing the FOV,
   rotating the local camera, ducking the volume or writing a screenshot copy all happen after
   the network layer and are never sent back. There is no auto-eat, no auto-click, no
   fastplace, no movement or hitbox modification - everything in this mod is presentation.
