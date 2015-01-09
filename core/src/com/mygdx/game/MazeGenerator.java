package com.mygdx.game;


import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class MazeGenerator {
    public  final  int widthInTiles;
    public final int heightInTiles;
    private final int widthInMazeCells;
    private final int heightInMazeCells;
    private final int[][] maze;
    private TiledMapTile empty;
    private TiledMapTile wallTiles[];
    private boolean[][] isWall;


    public MazeGenerator(int width, int height,TiledMapTile empty,TiledMapTile[] wallTiles) {
        widthInTiles = width;
        heightInTiles = height;
        isWall = new boolean[widthInTiles][heightInTiles];
        widthInMazeCells = (width - 1)/2;
        heightInMazeCells = (height - 1)/2;

        maze = new int[widthInMazeCells][heightInMazeCells];
        generateMaze(0, 0);
        this.empty = empty;
        this.wallTiles = wallTiles;

    }

    static class GenerateMazeCall {
        Direction[] dirs;
        int cx;
        int cy;
        int currentDir;

        GenerateMazeCall(int cx, int cy)
        {
            this.cx = cx;
            this.cy = cy;

            dirs = Direction.values();
            Collections.shuffle(Arrays.asList(dirs));

            currentDir = 0;
        }
    }

    private void generateMaze(int cx, int cy) {
        Stack<GenerateMazeCall> stack = new Stack<GenerateMazeCall>();
        stack.push(new GenerateMazeCall(cx, cy));

        while (!stack.isEmpty()) {
            GenerateMazeCall call = stack.peek();

            Direction dir = call.dirs[call.currentDir];

            if (call.currentDir == 3) {
                stack.pop();
            }

            int nx = call.cx + dir.dx;
            int ny = call.cy + dir.dy;

            if (between(nx, widthInMazeCells) && between(ny, heightInMazeCells)
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
        for (int i = 0; i < heightInMazeCells; i++) {
            for (int j = 0; j < widthInMazeCells; j++)
            {
                boolean hasNorthWall = (maze[j][i] & Direction.N.bit) == 0;
                boolean hasWestWall = (maze[j][i] & Direction.W.bit) == 0;
                boolean northernCellHasWestWall;
                if (between(j+ Direction.N.dx, widthInMazeCells) && between(i+ Direction.N.dy, heightInMazeCells))
                {
                    northernCellHasWestWall = (maze[j+ Direction.N.dx][i+ Direction.N.dy] & Direction.W.bit) == 0;
                }
                else
                {
                    northernCellHasWestWall = true;
                }

                boolean westernCellHasNorthernWall;
                if (between(j+ Direction.W.dx, widthInMazeCells) && between(i+ Direction.W.dy, heightInMazeCells))
                {
                    westernCellHasNorthernWall = (maze[j+ Direction.W.dx][i+ Direction.W.dy] & Direction.N.bit) == 0;
                }
                else
                {
                    westernCellHasNorthernWall = true;
                }

                boolean isSouthWestCorner = northernCellHasWestWall && westernCellHasNorthernWall;
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                isWall[j*2][i*2] = hasNorthWall||hasWestWall||isSouthWestCorner;
                isWall[j*2+1][i*2] = hasNorthWall;
                isWall[j*2][i*2+1] = hasWestWall;
                isWall[j*2+1][j*2+1] = false;


                //cell.setTile((hasNorthWall||hasWestWall||isSouthWestCorner) ?wall :empty);
                //layer.setCell(j * 2, i * 2, cell);
                //cell = new TiledMapTileLayer.Cell();
                //cell.setTile((hasNorthWall) ?wall :empty);
                //layer.setCell(j * 2 + 1, i * 2, cell);
                //cell = new TiledMapTileLayer.Cell();
                //cell.setTile((hasWestWall) ?wall :empty);
                //layer.setCell(j * 2, i * 2 + 1, cell);
                //cell = new TiledMapTileLayer.Cell();
                //cell.setTile(empty);
                //layer.setCell(j*2+1, i*2+1, cell);
            }
        }

        for (int x = 0; x < widthInTiles; x++) {
            isWall[x][heightInTiles-1]=true;
        }
        for (int y = 0; y < heightInTiles; y++) {
            isWall[widthInTiles-1][y]=true;
        }

        for (int x = 0; x < widthInTiles; x++) {
            for (int y = 0; y < heightInTiles; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                if (isWall[x][y]) {
                    int wallTileIndex = 0;
                    for(Direction dir:Direction.values()) {
                        if (between(x+ dir.dx, widthInTiles) && between(y+ dir.dy, heightInTiles) && isWall[x+dir.dx][y+dir.dy]){
                           wallTileIndex|=dir.bit;
                        }
                    }
                    cell.setTile(wallTiles[wallTileIndex]);
                }
                else{
                    cell.setTile(empty);
                }
                layer.setCell(x,y,cell);
            }
        }



    }


}
