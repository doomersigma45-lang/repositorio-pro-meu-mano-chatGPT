package com.example.examplemod;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MobsManeiros.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MobsManeiros.AZEITE.get(),c->new SkinRenderer<>(c,"azeite_de_oliva"));
        event.registerEntityRenderer(MobsManeiros.CACHORRO.get(),DogRenderer::new);
        event.registerEntityRenderer(MobsManeiros.EMPRESARIO.get(),c->new SkinRenderer<>(c,"empresario"));
        event.registerEntityRenderer(MobsManeiros.LENTO.get(),c->new SkinRenderer<>(c,"lento"));
        event.registerEntityRenderer(MobsManeiros.GOGOZONE.get(),c->new SkinRenderer<>(c,"gogo"));
    }
    private static class SkinRenderer<T extends net.minecraft.world.entity.Mob> extends MobRenderer<T,HumanoidModel<T>> {
        private final ResourceLocation texture;
        SkinRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,String name) {
            super(context,new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),.5f);
            texture=new ResourceLocation(MobsManeiros.ID,"textures/entity/"+name+".png");
        }
        @Override public ResourceLocation getTextureLocation(T entity) { return texture; }
    }
    private static class DogRenderer extends MobRenderer<MobsManeiros.DogMob,WolfModel<MobsManeiros.DogMob>> {
        private final ResourceLocation texture = new ResourceLocation(MobsManeiros.ID,"textures/entity/cachorro.png");
        DogRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
            super(context,new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)),.4f);
        }
        @Override public ResourceLocation getTextureLocation(MobsManeiros.DogMob entity) { return texture; }
    }
}
