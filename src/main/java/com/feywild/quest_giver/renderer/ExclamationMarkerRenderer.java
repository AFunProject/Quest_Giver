package com.feywild.quest_giver.renderer;

import com.feywild.quest_giver.QuestGiverMod;
import com.feywild.quest_giver.entity.QuestVillager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ExclamationMarkerRenderer {

    private static final float ICON_SCALE = 1.0F;
    private static final double MAX_DISTANCE = 64.0; // MAX BLOCK DISTANCE
    private static final double FADE_PERCENTAGE = 50.0; //this cannot be 100.0.
    private static final int Y_POS = -18;
    private static final int X_POS = -8;

    public static void renderExclamationMarker(EntityRenderer<?> renderer, Entity entity, Component component, PoseStack poseStack, MultiBufferSource buffer, int packedLight){

        if(entity instanceof QuestVillager){

            double squareDistance = renderer.entityRenderDispatcher.distanceToSqr(entity);
            double fadeDistance = ((1.0 - (FADE_PERCENTAGE / 100.0)) * MAX_DISTANCE);
            double opacityDistance =  Mth.clamp(1.0 - ((Math.sqrt(squareDistance) - fadeDistance) / (MAX_DISTANCE - fadeDistance)), 0.0, 1.0);
            float markerHeight = entity.getBbHeight() + 0.5F;

            if (squareDistance > MAX_DISTANCE * MAX_DISTANCE)
            {
                return; //STOP RENDERING MARK IF TO FAR AWAY FROM ENTITY
            }

            poseStack.pushPose();
            poseStack.translate(0.0D, (double)markerHeight, 0.0D);
            poseStack.mulPose(renderer.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float)opacityDistance );  // 1.0 is full

            renderMarker(getResourceLocation(), poseStack);

            poseStack.popPose();
        }
    }

    public static ResourceLocation getResourceLocation(){
        return new ResourceLocation(QuestGiverMod.getInstance().modid, "textures/entity/quest_villager/exclamation_mark.png");
    }

    private static void renderMarker(ResourceLocation resource, PoseStack poseStack){
        poseStack.pushPose();
        poseStack.scale(ICON_SCALE, ICON_SCALE, 1.0F);
        renderIcon(resource, poseStack);
        poseStack.popPose();
    }

    private static void renderIcon(ResourceLocation resource, PoseStack poseStack)
    {
        Matrix4f matrix = poseStack.last().pose();

        Minecraft.getInstance().getTextureManager().getTexture(resource).setFilter(false, false);
        RenderSystem.setShaderTexture(0, resource);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, (float)X_POS,			(float)(Y_POS + 16),		0).uv((float) 0, (float) 1).endVertex();
        bufferbuilder.vertex(matrix, (float)(X_POS + 16),	(float)(Y_POS + 16),		0).uv((float) 1, (float) 1).endVertex();
        bufferbuilder.vertex(matrix, (float)(X_POS + 16),	(float)Y_POS,			0).uv((float) 1, (float) 0).endVertex();
        bufferbuilder.vertex(matrix, (float)X_POS,			(float)Y_POS,			0).uv((float) 0, (float) 0).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

}