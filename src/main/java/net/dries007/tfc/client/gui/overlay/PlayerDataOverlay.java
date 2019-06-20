package net.dries007.tfc.client.gui.overlay;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;
import net.dries007.tfc.util.DamageManager;

import static net.dries007.tfc.api.util.TFCConstants.MOD_ID;

public final class PlayerDataOverlay
{
    private static final ResourceLocation ICONS = new ResourceLocation(MOD_ID, "textures/gui/overlay/icons.png");
    private FontRenderer fontrenderer = null;
    private static final PlayerDataOverlay INSTANCE = new PlayerDataOverlay();
    private float maxHealth=1000, curThirst=100;

    private PlayerDataOverlay(){}

    public static PlayerDataOverlay getInstance() { return INSTANCE; }

    public void setMaxHealth(float value) { this.maxHealth = value; }

    public void setCurThirst(float value) { this.curThirst = value; }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Pre event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player.inventory.player;
        GuiIngameForge.renderFood = false;
        // We check for crosshairs just because it's always drawn and is before air bar
        if(event.getType() != ElementType.CROSSHAIRS)
            return;

        // This is for air to be drawn above our bars
        GuiIngameForge.right_height += 10;

        ScaledResolution sr = event.getResolution();

        fontrenderer = mc.fontRenderer;

        int healthRowHeight = sr.getScaledHeight() - 40;
        int armorRowHeight = healthRowHeight - 10;
        int mid = sr.getScaledWidth() / 2;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(ICONS);

        if(mc.playerController.gameIsSurvivalOrAdventure())
        {
            //Draw Health
            this.drawTexturedModalRect(mid-91, healthRowHeight, 0, 0, 90, 10);
            float maxHealth = this.maxHealth;
            float curHealth = DamageManager.rescaleDamage(player.getHealth(), 20, maxHealth);
            float percentHealth = curHealth / maxHealth;
            this.drawTexturedModalRect(mid-91, healthRowHeight, 0, 10, (int) (90*percentHealth), 10);

            //Draw Food and Water
            float foodLevel = player.getFoodStats().getFoodLevel();
            float percentFood = foodLevel / 20f;
            float percentThirst = this.curThirst / 100f;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(mid+1, healthRowHeight, 0, 20, 90, 5);
            //Removed during port
			/*if(playerclient != null && playerclient.guishowFoodRestoreAmount)
			{
				float percentFood2 = Math.min(percentFood + playerclient.guiFoodRestoreAmount / foodstats.getMaxStomach(player), 1);
				GL11.glColor4f(0.0F, 0.6F, 0.0F, 0.3F);
				this.drawTexturedModalRect(mid+1, healthRowHeight, 0, 25, (int) (90*(percentFood2)), 5);
			}*/
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            this.drawTexturedModalRect(mid+1, healthRowHeight, 0, 25, (int) (90*percentFood), 5);

            this.drawTexturedModalRect(mid+1, healthRowHeight+5, 90, 20, 90, 5);
            this.drawTexturedModalRect(mid+1, healthRowHeight+5, 90, 25, (int) (90*percentThirst), 5);

            //Draw Notifications
            String healthString = ((int) Math.min(curHealth, maxHealth)) + "/" + ((int) (maxHealth));
            fontrenderer.drawString(healthString, mid-45-(fontrenderer.getStringWidth(healthString)/2), healthRowHeight+2, Color.white.getRGB());
            //Removed during port
            //if (player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getModifier(TFCAttributes.OVERBURDENED_UUID) != null)
            //	mc.fontrenderer.drawString(TFC_Core.translate("gui.overburdened"), mid-(mc.fontrenderer.getStringWidth(TFC_Core.translate("gui.overburdened"))/2), healthRowHeight-20, Color.red.getRGB());

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation("minecraft:textures/gui/icons.png"));

            //Draw experience bar when not riding anything, riding a non-living entity such as a boat/minecart, or riding a pig.
            if (!(player.getRidingEntity() instanceof EntityLiving))
            {
                int cap = 0;
                cap = player.xpBarCap();
                int left = mid - 91;

                if (cap > 0)
                {
                    short barWidth = 182;
                    int filled = (int) (player.experience * (barWidth + 1));
                    int top = sr.getScaledHeight() - 29;
                    drawTexturedModalRect(left, top, 0, 64, barWidth, 5);
                    if (filled > 0)
                        drawTexturedModalRect(left, top, 0, 69, filled, 5);
                }

                if (player.experienceLevel > 0)
                {
                    boolean flag1 = false;
                    int color = flag1 ? 16777215 : 8453920;
                    String text = Integer.toString(player.experienceLevel);
                    int x = (sr.getScaledWidth() - fontrenderer.getStringWidth(text)) / 2;
                    int y = sr.getScaledHeight() - 30;
                    fontrenderer.drawString(text, x + 1, y, 0);
                    fontrenderer.drawString(text, x - 1, y, 0);
                    fontrenderer.drawString(text, x, y + 1, 0);
                    fontrenderer.drawString(text, x, y - 1, 0);
                    fontrenderer.drawString(text, x, y, color);
                }

                // We have to reset the color back to white
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            // Don't show the dismount message if it was triggered by the "mounting" from opening a horse inventory.
			/*if (mc.currentScreen instanceof GuiScreenHorseInventoryTFC) //Removed during port
			{
				recordTimer = 0;
				try
				{
					_recordPlayingUpFor.setInt(mc.ingameGUI, 0);
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}*/

            // Draw mount's health bar
            if (player.getRidingEntity() instanceof EntityLivingBase)
            {
                GuiIngameForge.renderHealthMount = false;
                mc.renderEngine.bindTexture(ICONS);
                EntityLivingBase mount = ((EntityLivingBase) player.getRidingEntity());
                this.drawTexturedModalRect(mid+1, armorRowHeight, 90, 0, 90, 10);
                double mountMaxHealth = mount.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
                double mountCurrentHealth = mount.getHealth();
                float mountPercentHealth = (float)Math.min(mountCurrentHealth/mountMaxHealth, 1.0f);
                this.drawTexturedModalRect(mid+1, armorRowHeight, 90, 10, (int) (90*mountPercentHealth), 10);

                String mountHealthString = (int) Math.min(mountCurrentHealth, mountMaxHealth) + "/" + (int) mountMaxHealth;
                fontrenderer.drawString(mountHealthString, mid + 47 - (fontrenderer.getStringWidth(mountHealthString) / 2), armorRowHeight + 2, Color.white.getRGB());
                //renderDismountOverlay(mc, mid, sr.getScaledHeight(), event.partialTicks);//Removed during port
            }

            mc.renderEngine.bindTexture(new ResourceLocation("minecraft:textures/gui/icons.png"));
        }
    }

    public void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vb = tessellator.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(xCoord + 0.0F, yCoord + maxV, 0).tex((minU + 0) * f, (minV + maxV) * f1).endVertex();
        vb.pos(xCoord + maxU, yCoord + maxV, 0).tex((minU + maxU) * f, (minV + maxV) * f1).endVertex();
        vb.pos(xCoord + maxU, yCoord + 0.0F, 0).tex((minU + maxU) * f, (minV + 0) * f1).endVertex();
        vb.pos(xCoord + 0.0F, yCoord + 0.0F, 0).tex((minU + 0) * f, (minV + 0) * f1).endVertex();
        tessellator.draw();
    }
}
