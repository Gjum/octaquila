package gjum.minecraft.liteloader.octaquila;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.PostRenderListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "octaquila.json")
public class LiteModOctaquila implements PostRenderListener, Tickable {
    private static KeyBinding toggleShownKey = new KeyBinding("key.octaquila.toggleShown", Keyboard.KEY_O, "key.categories.litemods");
    private static KeyBinding toggleFixedHeightKey = new KeyBinding("key.octaquila.toggleFixedHeight", Keyboard.KEY_H, "key.categories.litemods");

    @Expose
    @SerializedName("edge")
    int edge = 34;

    @Expose
    @SerializedName("centerX")
    int centerX = 775;

    @Expose
    @SerializedName("centerZ")
    int centerZ = -76;

    @Expose
    @SerializedName("visibleRadius")
    int visibleRadius = 4 * edge;

    @Expose
    @SerializedName("visible")
    private boolean visible = true;

    @Expose
    @SerializedName("fixedHeight")
    private int fixedHeight = -1;

    /**
     * Default constructor. All LiteMods must have a default constructor. In general you should do very little
     * in the mod constructor EXCEPT for initialising any non-game-interfacing components or performing
     * sanity checking prior to initialisation
     */
    public LiteModOctaquila() {
    }

    @Override
    public String getName() {
        return "Aquila Octagon Grid Helper";
    }

    /**
     * getVersion() should return the same version string present in the mod metadata, although this is
     * not a strict requirement.
     *
     * @see com.mumfrey.liteloader.LiteMod#getVersion()
     */
    @Override
    public String getVersion() {
        return "0.1.1";
    }

    /**
     * init() is called very early in the initialisation cycle, before the game is fully initialised, this
     * means that it is important that your mod does not interact with the game in any way at this point.
     *
     * @see com.mumfrey.liteloader.LiteMod#init(java.io.File)
     */
    @Override
    public void init(File configPath) {
        LiteLoader.getInput().registerKeyBinding(LiteModOctaquila.toggleShownKey);
        LiteLoader.getInput().registerKeyBinding(LiteModOctaquila.toggleFixedHeightKey);
    }

    /**
     * upgradeSettings is used to notify a mod that its version-specific settings are being migrated
     *
     * @see com.mumfrey.liteloader.LiteMod#upgradeSettings(java.lang.String, java.io.File, java.io.File)
     */
    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (inGame && minecraft.currentScreen == null && Minecraft.isGuiEnabled()) {
            if (LiteModOctaquila.toggleShownKey.isPressed()) {
                visible = !visible;
                LiteLoader.getInstance().writeConfig(this);
                minecraft.thePlayer.addChatComponentMessage(
                        new TextComponentString("Octagon Helper " + (visible ? "shown" : "hidden"))
                                .setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_GRAY)));
            }
            if (LiteModOctaquila.toggleFixedHeightKey.isPressed()) {
                if (fixedHeight == -1)
                    fixedHeight = (int) minecraft.thePlayer.posY;
                else fixedHeight = -1;
                LiteLoader.getInstance().writeConfig(this);
                minecraft.thePlayer.addChatComponentMessage(
                        new TextComponentString("Octagon Helper height: " + (fixedHeight == -1 ? "follow player" : "at " + fixedHeight))
                                .setStyle(new Style().setItalic(true).setColor(TextFormatting.DARK_GRAY)));
            }
        }
    }

    @Override
    public void onPostRenderEntities(float partialTicks) {
    }

    @Override
    public void onPostRender(float partialTicks) {
        if (!visible) return;
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (p == null) return;

        glPushMatrix();
        double px = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double py = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double pz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;
        glTranslated(-px, -py, -pz);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int oy = fixedHeight == -1 ? (int) py : fixedHeight;

        for (int ox = nearestOctoCenter((int) px - visibleRadius, centerX);
             ox <= nearestOctoCenter((int) px + visibleRadius, centerX);
             ox += edge * 4) {
            for (int oz = nearestOctoCenter((int) pz - visibleRadius, centerZ);
                 oz <= nearestOctoCenter((int) pz + visibleRadius, centerZ);
                 oz += edge * 4) {
                // centered
                glColor3f(0, 1, 0);
                new Octagon(ox, oy, oz).render();
                // offset
                glColor3f(0, 0, 1);
                new Octagon(ox + 2 * edge, oy, oz + 2 * edge).render();
            }
        }

        // cross in nearest octo center
        glColor3f(1, 0, 0);
        int ocx = nearestOctoCenter((int) px, centerX);
        int ocz = nearestOctoCenter((int) pz, centerZ);
        new Octagon(ocx, oy, ocz).render();
        glBegin(GL_LINES);
        glVertex3f(ocx, oy, ocz);
        glVertex3f(ocx + 1f, oy, ocz + 1f);
        glVertex3f(ocx + 1f, oy, ocz);
        glVertex3f(ocx, oy, ocz + 1f);
        glEnd();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    int nearestOctoCenter(int coord, int center) {
        int interval = 4 * edge;
        int off = (coord - center - interval / 2) % interval;
        if (off < 0) off = interval + off;
        return coord - off + interval / 2;
    }

    private class Octagon {
        final int ri = edge / 2;
        final int ro = ri * 3;

        final float x;
        final float y;
        final float z;

        Octagon(int x, float y, int z) {
            this.x = x + .5f;
            this.y = y + .5f;
            this.z = z + .5f;
        }

        void render() {
            glBegin(GL_LINE_LOOP);
            glVertex3f(x - ri, y, z - ro);
            glVertex3f(x + ri, y, z - ro);
            glVertex3f(x + ro, y, z - ri);
            glVertex3f(x + ro, y, z + ri);
            glVertex3f(x + ri, y, z + ro);
            glVertex3f(x - ri, y, z + ro);
            glVertex3f(x - ro, y, z + ri);
            glVertex3f(x - ro, y, z - ri);
            glEnd();
        }
    }
}
