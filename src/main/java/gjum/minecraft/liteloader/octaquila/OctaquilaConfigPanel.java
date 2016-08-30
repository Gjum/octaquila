package gjum.minecraft.liteloader.octaquila;

import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.LinkedList;


public class OctaquilaConfigPanel extends Gui implements ConfigPanel {
    private LinkedList<GuiButton> buttons = new LinkedList<GuiButton>();
    private HashMap<Integer, Runnable> buttonHandlers = new HashMap<Integer, Runnable>();

    /**
     * configuration panel class must have a default (no-arg) constructor
     */
    public OctaquilaConfigPanel() {
    }

    @Override
    public String getPanelTitle() {
        return "Block Highlighter Config";
    }

    @Override
    public int getContentHeight() {
        return getRowHeight() * buttons.size();
    }

    private static int BUTTON_HEIGHT = 20;

    private int getRowHeight() {
        int fontH = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        return (int) (1.1 * Math.max(fontH, BUTTON_HEIGHT));
    }

    @Override
    public void onPanelShown(ConfigPanelHost host) {
        int controlId = 0;
        for (String s : new LinkedList<String>()) {
            final GuiButton button = new GuiButton(controlId, 135, (controlId + 1) * getContentHeight(), 180, BUTTON_HEIGHT, s);
            buttons.add(button);

            Runnable handler = new Runnable() {
                @Override
                public void run() {
                    // Clear any pre-accumulated mouse wheel delta as it will be detected in onTick() as an attempt to bind to the scroll wheel.
                    Mouse.getDWheel();

//                    focusedControl = button;
//                    button.enabled = false;
//                    button.displayString = "Press a key or mouse combination.";
                }
            };
            buttonHandlers.put(controlId, handler);
            ++controlId;
        }
    }

    @Override
    public void onPanelResize(ConfigPanelHost host) {

    }

    @Override
    public void onPanelHidden() {

    }

    @Override
    public void onTick(ConfigPanelHost host) {

    }

    @Override
    public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int labelHeight = (int) (0.75 * fr.FONT_HEIGHT);
        drawCenteredString(fr, "Foo Bar", host.getWidth() / 2, labelHeight, 0xFFFFFF55);

        for (GuiButton button : buttons) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    @Override
    public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {
        Minecraft mc = Minecraft.getMinecraft();
        for (GuiButton control : buttons) {
            // Note: GulButton.mousePressed() just returns true if the control is
            // enabled and visible, and the mouse coords are within the control.
            if (control.mousePressed(mc, mouseX, mouseY)) {
                control.playPressSound(mc.getSoundHandler());
                Runnable handler = buttonHandlers.get(control.id);
                if (handler != null) {
                    handler.run();
                }
                break;
            }
        }
    }

    @Override
    public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY) {

    }

    @Override
    public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            host.close();
        }
    }
}
