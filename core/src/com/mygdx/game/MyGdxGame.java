package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import  com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import javax.swing.CellEditor;

import javafx.scene.control.Cell;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
    TiledMap tiledMap;
    OrthographicCamera camera;
    OrthogonalTiledMapRendererWithSprites tiledMapRenderer;
    float playerVelocity;
    int oldx,oldy;
    boolean firsttouch, playerMove;
    float w,h, maxScrollX, maxScrollY;
    Sprite playerSprite;
    int tileWidth,tileHeight,tiledMapWidth,tiledMapHeight;
    Texture texture;
    @Override
    public void create () {
        tileWidth=64;
        tileHeight=64;
        tiledMapHeight = 100;
        tiledMapWidth = 100;
        playerVelocity = 0.2f;
        Texture tiles = new Texture(Gdx.files.internal("AMazeingTileset.png"));
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, tileWidth, tileHeight);
        StaticTiledMapTile wall = new StaticTiledMapTile(splitTiles[1][0]);
        wall.setId(2);
        StaticTiledMapTile empty = new StaticTiledMapTile(splitTiles[0][0]);
        empty.setId(1);
        StaticTiledMapTile blue = new StaticTiledMapTile(splitTiles[0][1]);
        blue.setId(3);
        StaticTiledMapTile green = new StaticTiledMapTile(splitTiles[1][1]);
        green.setId(4);
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        camera.update();
        //tiledMap = new TmxMapLoader().load("TilesetTest2.tmx");
        MazeGenerator mazeGenerator = new MazeGenerator(tiledMapWidth,tiledMapHeight,wall,empty,green,blue);
        tiledMap = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(tiledMapWidth,tiledMapHeight,tileWidth,tileHeight);
        mazeGenerator.fillMapLayer(layer);
        tiledMap.getLayers().add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(tiledMap);
        Gdx.input.setInputProcessor(this);
        tiledMap.getLayers().get(0).setVisible(true);
        maxScrollX = w - (getTileWidth() *tiledMapWidth);
        maxScrollY = h - (tileHeight*tiledMapHeight);
        texture = new Texture(Gdx.files.internal("PlayerSprite.png"));
        playerSprite = new Sprite(texture);
        tiledMapRenderer.addSprite(playerSprite);
        //MapProperties startPos = tiledMap.getLayers().get(1).getObjects().get("startPos").getProperties();
        playerSprite.setPosition(/*(Float)startPos.get("x")*/ tileWidth  ,/*(Float)startPos.get("y")*/ tileHeight );
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 screen = camera.unproject(new Vector3 (screenX,screenY,0));
        firsttouch = true;
        if ((new Vector2(screen.x,screen.y).dst(playerSprite.getX(),playerSprite.getY())) < 150) {
            playerMove = true;

        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        playerMove = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 screen = camera.unproject(new Vector3(screenX,screenY,0));
        if (!firsttouch) {
            if (playerMove) {
                TiledMapTileLayer layer = (TiledMapTileLayer)tiledMap.getLayers().get(0);
                int playerDestY = (int) (screen.y);
                int playerDestX = (int) (screen.x);
                int playerX = (int) playerSprite.getX();
                int playerY = (int) playerSprite.getY();
                int moveRoundCounter;
                moveRoundCounter = 0;
                while (playerX != playerDestX || playerY != playerDestY)
                {
                    playerX+= Math.signum(playerDestX - playerX);
                    playerY+= Math.signum(playerDestY - playerY);

                    if (isBlocked(layer, playerY, playerX)||
                        isBlocked(layer, (playerY+texture.getWidth()), playerX)||
                        isBlocked(layer, playerY, (playerX+texture.getWidth()))||
                        isBlocked(layer, playerY+texture.getHeight(), (playerX+texture.getWidth()))||
                                 (moveRoundCounter>playerVelocity/Gdx.graphics.getDeltaTime()))
                    {

                            break;
                    }
                    else
                    {
                        playerSprite.setPosition(playerX, playerY);
                        moveRoundCounter++;
                    }
                }


            if (playerSprite.getX()<camera.position.x-camera.viewportWidth/2+(w/10) ){
                camera.translate(-50,0);

            }
            if (playerSprite.getX()>camera.position.x-camera.viewportWidth/2+((w/20)*18)){
                camera.translate(50,0);
            }
            if (playerSprite.getY()<camera.position.y-camera.viewportHeight/2+(h/10) ){
                camera.translate(0,-50);
            }
            if (playerSprite.getY()>camera.position.y-camera.viewportHeight/2+((h/20)*18)){
                camera.translate(0,50);
            }



            }
            else {
                camera.translate(-(screenX - oldx), (screenY - oldy));



            }
            Vector3 translation = camera.position;
            translation.x = Math.max(w / 2, translation.x);
            translation.y = Math.max(h / 2, translation.y);

            translation.x = Math.min(-maxScrollX + w / 2, translation.x);
            translation.y = Math.min(-maxScrollY + h / 2, translation.y);
        }
            else firsttouch = false;
        oldx = screenX;oldy = screenY;
        return false;
    }

    private Integer getTileWidth() {
        return tileWidth;
    }

    private boolean isBlocked(TiledMapTileLayer layer, int y, int x) {
        x/=tileHeight;
        y/=tileWidth;
        if (layer.getCell(x, y) == null) return true;
        return layer.getCell(x, y).getTile().getId() == 2;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
