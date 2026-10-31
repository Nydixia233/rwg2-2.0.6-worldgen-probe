package net.minecraft.world.gen.feature;
import java.util.Random; import net.minecraft.world.World;
public abstract class WorldGenerator {
public abstract boolean generate(World w, Random r, int x,int y,int z);
public void setScale(double a,double b,double c){} }