package rwg2.deco.trees;
import java.util.*; import java.lang.reflect.*;
import net.minecraft.init.Blocks; import net.minecraft.block.Block;
import net.minecraft.world.World; import net.minecraft.world.biome.BiomeGenBase;
import rwg2.surface.*; import rwg2.util.*; import rwg2.map.LavaCaveLandmark;
public class Probe {
 static final NoiseGenerator ZERO=new NoiseGenerator(){
  public double improvedNoise(double x,double y,double z){return 0;}
  public float noise1(float x){return 0;} public float noise2(float x,float y){return 0;}
  public float noise3(float x,float y,float z){return 0;}
 };
 static void ok(boolean value,String msg){if(!value)throw new AssertionError(msg);System.out.println("PASS "+msg);}
 static Block[] column(int height){Block[] b=new Block[65536];Arrays.fill(b,Blocks.air);for(int y=0;y<=height;y++)b[y]=Blocks.stone;return b;}
 static void paint(Block[] b){new SurfaceGrassland(Blocks.grass,Blocks.dirt,Blocks.stone,Blocks.cobblestone)
   .paintTerrain(b,new byte[65536],0,0,0,0,-1,new World(),new Random(1),ZERO,new CellNoise(),new float[256],0,new BiomeGenBase[256]);}
 static DecoJungleFat jungle(){return new DecoJungleFat(Blocks.log,3,Blocks.leaves,3,20,5,14,3,.2f,.25f);}
 public static void main(String[] args)throws Exception {
  Block[] generic=column(100);for(int y=71;y<=80;y++)generic[y]=Blocks.air;paint(generic);
  ok(generic[70]==Blocks.grass,"generic enclosed cavity at Y71..80: hidden floor Y70 painted grass");
  Block[] river=column(100);for(int y=58;y<=73;y++)river[y]=y<=62?Blocks.water:Blocks.air;paint(river);
  ok(river[57]==Blocks.dirt,"actual river-like floor<=62: bed Y57 painted dirt, not grass");
  float[] noise=new float[256];Arrays.fill(noise,70);
  float edge=CliffCalculator.calc(15,8,noise);noise[8*16+15]=80;
  float inner=CliffCalculator.calc(14,8,noise);
  ok(edge==0&&inner==10,"cliff: same +10 external step is invisible at local x15, visible at x14");
  World wall=new World();wall.blocks.put(wall.key(0,64,0),Blocks.grass);
  for(int x=-20;x<=20;x++)for(int y=65;y<=110;y++)for(int z=1;z<=20;z++)wall.blocks.put(wall.key(x,y,z),Blocks.stone);
  boolean generated=jungle().generate(wall,new Random(3),0,65,0);
  ok(generated&&wall.stoneOverwrites>0&&wall.getBlock(1,70,1)==Blocks.log,"JungleFat returned true after overwriting stone: "+wall.stoneOverwrites+"; trunk replaced stone at 1,70,1");
  World sky=new World();sky.blocks.put(sky.key(0,249,0),Blocks.grass);
  boolean high=jungle().generate(sky,new Random(3),0,250,0);
  ok(high&&sky.invalidWrites>0,"JungleFat returned true despite out-of-height writes: "+sky.invalidWrites);
  DecoBigTree tree=new DecoBigTree(10,0);tree.worldObj=new World();
  int[] a={1,65,1},b={4,69,3};tree.worldObj.blocks.put(tree.worldObj.key(2,66,2),Blocks.stone);
  int check=tree.checkBlockLine(a,b);tree.placeBlockLine(a,b,Blocks.log);
  ok(check==-1&&tree.worldObj.getBlock(2,66,2)==Blocks.log,"BigTree check floor differs from placement rounding: missed stone at 2,66,2 overwritten");
  Set<Integer> sizes=new TreeSet<>();for(int i=0;i<128;i++)sizes.add(new DecoBigTree().size);
  ok(sizes.size()>1,"BigTree defaults sampled without supplied seed: observed sizes "+sizes);
  DecoJungleFat cast=jungle();World negative=new World();
  cast.generateBranch(negative,new Random(1),-.5f,70,-.5f,0,0,1,1);
  ok(negative.getBlock(0,70,0)==Blocks.log&&negative.isAirBlock(-1,70,-1),"JungleFat negative coordinate -.5 truncates to 0 instead of floor -1");
  Method carve=LavaCaveLandmark.class.getDeclaredMethod("carveColumn",Block[].class,byte[].class,int.class,int.class,int.class,int.class,float.class,float.class,NoiseGenerator.class,CellNoise.class,double[].class);
  carve.setAccessible(true);
  int softShelves=0,sandShelves=0;String first="";
  for(int d=81;d<=139;d++){
   float distance=d/10f;
   for(boolean sand:new boolean[]{false,true}){
    Block[] col=column(70);for(int y=65;y<=70;y++)col[y]=sand?Blocks.sand:(y==70?Blocks.grass:Blocks.dirt);
    carve.invoke(null,col,new byte[65536],0,0,1,1,distance,0f,ZERO,new CellNoise(),new double[4]);
    for(int y=60;y<70;y++)if((col[y]==Blocks.dirt||col[y]==Blocks.sand)&&col[y-1]==Blocks.air){
      if(sand)sandShelves++;else {softShelves++;if(first.isEmpty())first="distance="+distance+", y="+y;}
    }
   }
  }
  ok(softShelves>0,"lava funnel leaves soil undersides after initial support pass: "+softShelves+"; first "+first);
  ok(sandShelves>0,"lava funnel leaves unsupported sand after BlockFalling support pass: "+sandShelves);
  World crossed=new World();crossed.blocks.put(crossed.key(23,64,23),Blocks.grass);
  jungle().generate(crossed,new Random(3),23,65,23);
  Set<String> chunks=new TreeSet<>();for(String key:crossed.blocks.keySet()){
   String[] p=key.split(",");chunks.add((Integer.parseInt(p[0])>>4)+","+(Integer.parseInt(p[2])>>4));}
  boolean outside=false;for(String chunk:chunks){String[] p=chunk.split(",");int x=Integer.parseInt(p[0]),z=Integer.parseInt(p[1]);if(x<0||x>1||z<0||z>1)outside=true;}
  ok(outside,"JungleFat at population edge x=z=23 writes beyond chunks [0..1]^2: "+chunks);
 }
}