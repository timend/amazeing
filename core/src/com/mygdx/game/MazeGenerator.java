package com.mygdx.game;


import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.Vector;

public class MazeGenerator {

    private final int width;
    private final int height;
    private final int[][] maze;
    private TiledMapTile wall, empty, green, blue;


    public MazeGenerator(int width, int height, TiledMapTile wall, TiledMapTile empty, TiledMapTile green, TiledMapTile blue) {
        this.width = width/2;
        this.height = height/2;
        maze = new int[this.width][this.height];
        generateMaze(0, 0);
        this.wall = wall;
        this.empty = empty;
        this.green = green;
        this.blue = blue;
    }

    static class GenerateMazeCall {
        DIR[] dirs;
        int cx;
        int cy;
        int currentDir;

        GenerateMazeCall(int cx, int cy)
        {
            this.cx = cx;
            this.cy = cy;

            dirs = DIR.values();
            Collections.shuffle(Arrays.asList(dirs));

            currentDir = 0;
        }
    }

    private void generateMaze(int cx, int cy) {
        Stack<GenerateMazeCall> stack = new Stack<GenerateMazeCall>();
        stack.push(new GenerateMazeCall(cx, cy));

        while (!stack.isEmpty()) {
            GenerateMazeCall call = stack.peek();

            DIR dir = call.dirs[call.currentDir];

            if (call.currentDir == 3) {
                stack.pop();
            }

            int nx = call.cx + dir.dx;
            int ny = call.cy + dir.dy;

            if (between(nx, width) && between(ny, height)
                    && (maze[nx][ny] == 0)) {
                maze[call.cx][call.cy] |= dir.bit;
                maze[nx][ny] |= dir.opposite.bit;
                stack.push(new GenerateMazeCall(nx, ny));
            }

            call.currentDir++;
        }
    }

    private static boolean between(int v, int upper) {
        return (v >= 0) && (v < upper);
    }

    public void fillMapLayer(TiledMapTileLayer layer){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++)
            {
                boolean hasNorthWall = (maze[j][i] & DIR.N.bit) == 0;
                boolean hasWestWall = (maze[j][i] & DIR.W.bit) == 0;
                boolean northernCellHasWestWall;
                if (between(j+DIR.N.dx, width) && between(i+DIR.N.dy, height))
                {
                    northernCellHasWestWall = (maze[j+DIR.N.dx][i+DIR.N.dy] & DIR.W.bit) == 0;
                }
                else
                {
                    northernCellHasWestWall = true;
                }

                boolean westernCellHasNorthernWall;
                if (between(j+DIR.W.dx, width) && between(i+DIR.W.dy, height))
                {
                    westernCellHasNorthernWall = (maze[j+DIR.W.dx][i+DIR.W.dy] & DIR.N.bit) == 0;
                }
                else
                {
                    westernCellHasNorthernWall = true;
                }

                boolean isSouthWestCorner = northernCellHasWestWall && westernCellHasNorthernWall;
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile((hasNorthWall||hasWestWall||isSouthWestCorner) ?wall :empty);
                layer.setCell(j * 2, i * 2, cell);
                cell = new TiledMapTileLayer.Cell();
                cell.setTile((hasNorthWall) ?wall :empty);
                layer.setCell(j * 2 + 1, i * 2, cell);
                cell = new TiledMapTileLayer.Cell();
                cell.setTile((hasWestWall) ?wall :empty);
                layer.setCell(j * 2, i * 2 + 1, cell);
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(empty);
                layer.setCell(j*2+1, i*2+1, cell);
            }
        }
    }


    private enum DIR {
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
        private final int bit;
        private final int dx;
        private final int dy;
        private DIR opposite;

        // use the static initializer to resolve forward references
        static {
            N.opposite = S;
            S.opposite = N;
            E.opposite = W;
            W.opposite = E;
        }

        private DIR(int bit, int dx, int dy) {
            this.bit = bit;
            this.dx = dx;
            this.dy = dy;
        }
    }
}
