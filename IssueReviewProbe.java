import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import rwg2.map.LavaCaveLandmark;
import rwg2.surface.SurfaceGrassland;
import rwg2.util.CellNoise;
import rwg2.util.NoiseGenerator;

/** Supplemental algorithm-only review using the existing unmodified 2.0.6 probe classes. */
public class IssueReviewProbe {
    private static final NoiseGenerator ZERO = new NoiseGenerator() {
        public double improvedNoise(double x, double y, double z) { return 0; }
        public float noise1(float x) { return 0; }
        public float noise2(float x, float z) { return 0; }
        public float noise3(float x, float y, float z) { return 0; }
    };

    private static Block[] column(int height) {
        Block[] blocks = new Block[65536];
        Arrays.fill(blocks, Blocks.air);
        for (int y = 0; y <= height; y++) blocks[y] = Blocks.stone;
        return blocks;
    }

    private static void require(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        System.out.println("PASS " + label);
    }

    public static void main(String[] args) throws Exception {
        Method carve = LavaCaveLandmark.class.getDeclaredMethod("carveColumn",
            Block[].class, byte[].class, int.class, int.class, int.class, int.class,
            float.class, float.class, NoiseGenerator.class, CellNoise.class, double[].class);
        carve.setAccessible(true);
        // With zero noise, distance=8.1 and surface=70, the pre-funnel top is 62.
        for (int sandStart : new int[] {65, 63}) {
            Block[] blocks = column(70);
            for (int y = sandStart; y <= 70; y++) blocks[y] = Blocks.sand;
            Block beforeTopPlusOne = blocks[63];
            carve.invoke(null, blocks, new byte[65536], 0, 0, 1, 1, 8.1f, 0f,
                ZERO, new CellNoise(), new double[4]);
            require(blocks[67] == Blocks.sand && blocks[66] == Blocks.air,
                "sandStart=" + sandStart + ": new unsupported sand at Y67 after funnel");
            if (sandStart == 65) {
                require(beforeTopPlusOne == Blocks.stone && blocks[62] == Blocks.air,
                    "original six-layer fixture: support branch never entered (top+1 was stone)");
            } else {
                require(beforeTopPlusOne == Blocks.sand && blocks[62] == Blocks.stone,
                    "eight-layer fixture: support at top=62 remains intact after funnel");
            }
            System.out.println("Y62..70=" + Arrays.toString(Arrays.copyOfRange(blocks, 62, 71)));
        }

        SurfaceGrassland surface = new SurfaceGrassland(
            Blocks.grass, Blocks.dirt, Blocks.stone, Blocks.cobblestone);
        Block[] cavity = column(100);
        for (int y = 71; y <= 80; y++) cavity[y] = Blocks.air;
        surface.paintTerrain(cavity, new byte[65536], 0, 0, 0, 0, -1, new World(),
            new Random(1), ZERO, new CellNoise(), new float[256], 0, new BiomeGenBase[256]);
        require(cavity[70] == Blocks.grass && cavity[81] == Blocks.stone,
            "same enclosed cavity: floor Y70 is grass, ceiling underside Y81 remains stone");

        Block[] floor62 = column(100);
        for (int y = 63; y <= 73; y++) floor62[y] = Blocks.air;
        surface.paintTerrain(floor62, new byte[65536], 0, 0, 0, 0, -1, new World(),
            new Random(1), ZERO, new CellNoise(), new float[256], 0, new BiomeGenBase[256]);
        require(floor62[62] == Blocks.grass,
            "a solid floor at Y62 takes the top-block branch; actual river solid bed is <=61");
    }
}
