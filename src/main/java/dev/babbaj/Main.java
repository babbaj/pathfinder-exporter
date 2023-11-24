package dev.babbaj;

import dev.babbaj.pathfinder.NetherPathfinder;
import dev.babbaj.pathfinder.PathSegment;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Main {
    private static final int NUM_X_BITS = 26;
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;

    private static int x(long packed) {
        return (int) (packed << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
    }
    private static int y(long packed) {
        return (int) (packed << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
    }
    private static int z(long packed) {
        return (int) (packed << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("<cache path> <x1> <z1> <x2> <z2>");
            System.exit(1);
        }
        final String cachePath = args[0];
        final int x1 = Integer.parseInt(args[1]);
        final int z1 = Integer.parseInt(args[2]);
        final int x2 = Integer.parseInt(args[3]);
        final int z2 = Integer.parseInt(args[4]);

        final long ctx = NetherPathfinder.newContext(0, cachePath);
        int startX = x1;
        int startZ = z1;
        PathSegment segment;
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get("out.path"))))) {
            do {
                segment = NetherPathfinder.pathFind(ctx, startX, 64, startZ, x2, 64, z2, true, false, 0, true, 1000);
//                System.out.println("segment " + segment.finished);
                if (segment == null) {
                    System.err.println("pathFind returned null");
                    System.exit(1);
                }
                long last = segment.packed[segment.packed.length - 1];
                startX = x(last);
                startZ = z(last);
                for (long l : segment.packed) {
                    dos.writeInt(x(l));
                    dos.writeInt(y(l));
                    dos.writeInt(z(l));
                }
            } while (!segment.finished);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
