package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod(MobsManeiros.ID)
public class MobsManeiros {
    public static final String ID = "mobsmaneiros";
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    public static final RegistryObject<EntityType<HumanoidMob>> AZEITE = human("azeite_de_oliva", MobCategory.CREATURE, .25f);
    public static final RegistryObject<EntityType<DogMob>> CACHORRO = TYPES.register("cachorro", () -> EntityType.Builder.of(DogMob::new, MobCategory.CREATURE).sized(.7f,.85f).build(ID+":cachorro"));
    public static final RegistryObject<EntityType<HumanoidMob>> EMPRESARIO = human("empresario", MobCategory.CREATURE, .25f);
    public static final RegistryObject<EntityType<HumanoidMob>> LENTO = human("lento", MobCategory.MONSTER, .38f);
    public static final RegistryObject<EntityType<HumanoidMob>> GOGOZONE = human("gogozone", MobCategory.MONSTER, .55f);
    private static RegistryObject<EntityType<HumanoidMob>> human(String id, MobCategory category, float speed) {
        return TYPES.register(id, () -> EntityType.Builder.of((EntityType<HumanoidMob> type, Level level) -> new HumanoidMob(type,level), category).sized(.6f,1.95f).build(ID+":"+id));
    }
    public MobsManeiros() { TYPES.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus()); }

    public static class HumanoidMob extends PathfinderMob {
        public HumanoidMob(EntityType<? extends PathfinderMob> type, Level level) { super(type,level); }
        @Override protected void registerGoals() {
            boolean hostile = getType()==LENTO.get() || getType()==GOGOZONE.get();
            goalSelector.addGoal(0,new FloatGoal(this));
            if (hostile) { goalSelector.addGoal(1,new MeleeAttackGoal(this,getType()==GOGOZONE.get()?1.8:1.35,true)); targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true)); }
            else { goalSelector.addGoal(1,new RandomStrollGoal(this,1)); goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false)); targetSelector.addGoal(1,new HurtByTargetGoal(this)); }
            goalSelector.addGoal(3,new LookAtPlayerGoal(this,Player.class,8)); goalSelector.addGoal(4,new RandomLookAroundGoal(this));
        }
        @Override public boolean isAggressive() { return getType()==LENTO.get() || getType()==GOGOZONE.get(); }
        @Override public void tick() { super.tick(); if(!level().isClientSide && getType()==LENTO.get() && getMainHandItem().isEmpty()) setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD)); }
    }
    public static class DogMob extends TamableAnimal {
        protected DogMob(EntityType<? extends TamableAnimal> type, Level level) { super(type,level); }
        @Override protected void registerGoals() {
            goalSelector.addGoal(0,new FloatGoal(this)); goalSelector.addGoal(1,new SitWhenOrderedToGoal(this)); goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true)); goalSelector.addGoal(3,new FollowOwnerGoal(this,1.1,5,2)); goalSelector.addGoal(4,new BreedGoal(this,1)); goalSelector.addGoal(5,new TemptGoal(this,1,Ingredient.of(Items.BEEF,Items.COOKED_BEEF,Items.PORKCHOP,Items.COOKED_PORKCHOP),false)); goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,1)); targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));
        }
        @Override public boolean isFood(ItemStack stack) { return stack.is(Items.BEEF)||stack.is(Items.COOKED_BEEF)||stack.is(Items.PORKCHOP)||stack.is(Items.COOKED_PORKCHOP)||stack.is(Items.CHICKEN)||stack.is(Items.COOKED_CHICKEN); }
        @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
            ItemStack stack=player.getItemInHand(hand);
            if(!isTame() && stack.is(Items.BONE)) { if(!level().isClientSide){ if(random.nextInt(3)==0){tame(player);navigation.stop();setTarget(null);setOrderedToSit(true);level().broadcastEntityEvent(this,(byte)7);}else level().broadcastEntityEvent(this,(byte)6);if(!player.getAbilities().instabuild)stack.shrink(1);} return InteractionResult.sidedSuccess(level().isClientSide); }
            if(isTame() && isFood(stack) && !isBaby()) return super.mobInteract(player,hand);
            return super.mobInteract(player,hand);
        }
        @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) { return CACHORRO.get().create(level); }
    }
    @Mod.EventBusSubscriber(modid=ID,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent public static void spawns(SpawnPlacementRegisterEvent event) {
            event.register(AZEITE.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Mob::checkMobSpawnRules,SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CACHORRO.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Mob::checkMobSpawnRules,SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(EMPRESARIO.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Mob::checkMobSpawnRules,SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(LENTO.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,random)->!w.canSeeSky(p)&&w.getMaxLocalRawBrightness(p)<=7&&Mob.checkMobSpawnRules(t,w,r,p,random),SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(GOGOZONE.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,random)->(!w.canSeeSky(p)||w.getBiome(p).is(BiomeTags.IS_FOREST))&&w.getMaxLocalRawBrightness(p)<=7&&Mob.checkMobSpawnRules(t,w,r,p,random),SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
        @SubscribeEvent public static void attributes(EntityAttributeCreationEvent event) {
            event.put(AZEITE.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.MOVEMENT_SPEED,.25).build());
            event.put(EMPRESARIO.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,150).add(Attributes.ATTACK_DAMAGE,50).add(Attributes.MOVEMENT_SPEED,.25).build());
            event.put(LENTO.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,60).add(Attributes.ATTACK_DAMAGE,5.5).add(Attributes.MOVEMENT_SPEED,.38).build());
            event.put(GOGOZONE.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,300).add(Attributes.ATTACK_DAMAGE,15).add(Attributes.MOVEMENT_SPEED,.55).build());
            event.put(CACHORRO.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,17).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.MOVEMENT_SPEED,.3).build());
        }
    }
    @Mod.EventBusSubscriber(modid=ID)
    public static class GameplayEvents {
        @SubscribeEvent public static void drops(LivingDropsEvent event) { if(event.getEntity().getType()==EMPRESARIO.get()){event.getEntity().spawnAtLocation(Items.DIAMOND,1+event.getEntity().getRandom().nextInt(3));event.getEntity().spawnAtLocation(Items.EMERALD,1+event.getEntity().getRandom().nextInt(4));} }
    }
}
