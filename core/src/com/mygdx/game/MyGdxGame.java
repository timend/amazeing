package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor, GestureDetector.GestureListener {
    public static final String BLOCKING_PROPERTY = "wall";
    TiledMap tiledMap;
    OrthographicCamera camera;
    OrthogonalTiledMapRendererWithSprites tiledMapRenderer;
    float playerVelocity;
    int oldx,oldy;
    boolean firsttouch, playerMove;
    float screenWidth, screenHeight, minZoom;
    Sprite playerSprite;
    int tileWidth,tileHeight,tiledMapWidth,tiledMapHeight;
    Texture texture;
    TiledMapTile[] wallTiles;
    float zoomSensitivity = 0.5f;

    @Override
    public void create () {
        tileWidth=64;
        tileHeight=64;
        tiledMapHeight = 51;
        tiledMapWidth = 51;
        playerVelocity = 0.2f;
        wallTiles = new TiledMapTile[16];
        Texture tiles = new Texture(Gdx.files.internal("AMazeingTileset.png"));
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, tileWidth, tileHeight);
        int[] colDirections={0, Direction.E.bit,Direction.S.bit, Direction.E.bit|Direction.S.bit};
        int[] rowDirections={0, Direction.W.bit,Direction.N.bit, Direction.W.bit|Direction.N.bit};
        for (int i = 0;i<4;i++){
            for (int i2 = 0; i2<4;i2++){
                TiledMapTile tile = new StaticTiledMapTile(splitTiles[i][i2]);
                tile.getProperties().put(BLOCKING_PROPERTY,true);
                wallTiles[rowDirections[i]|colDirections[i2]] =tile;
            }

        }
        StaticTiledMapTile empty = new StaticTiledMapTile(splitTiles[0][0]);
        empty.setId(1);
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();
        //tiledMap = new TmxMapLoader().load("TilesetTest2.tmx");
        MazeGenerator mazeGenerator = new MazeGenerator(tiledMapWidth,tiledMapHeight,empty,wallTiles);
        tiledMap = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(tiledMapWidth,tiledMapHeight,tileWidth,tileHeight);
        mazeGenerator.fillMapLayer(layer);
        tiledMap.getLayers().add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(tiledMap);
        GestureDetector gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(gestureDetector, this));
        tiledMap.getLayers().get(0).setVisible(true);
        minZoom = Math.min(((float) getTileWidth() * tiledMapWidth / (float) screenWidth),
                ((float) tileHeight * tiledMapHeight / (float) screenHeight));
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
                while ((playerX != playerDestX || playerY != playerDestY) &&
                        !(moveRoundCounter > playerVelocity / Gdx.graphics.getDeltaTime()))
                {
                    int dx = (int)Math.signum(playerDestX - playerX);
                    int dy = (int)Math.signum(playerDestY - playerY);

                    if (!isPlayerBlocked(layer, playerX + dx, playerY + dy))
                    {
                        playerX += dx;
                        playerY += dy;
                    }
                    else if (!isPlayerBlocked(layer, playerX, playerY + dy))
                    {
                        playerY += dy;
                    }
                    else if (!isPlayerBlocked(layer, playerX + dx, playerY))
                    {
                        playerX += dx;
                    }
                    else
                    {
                        break;
                    }
                    moveRoundCounter++;
                }
                playerSprite.setPosition(playerX, playerY);
                if (playerSprite.getX()<camera.position.x-camera.viewportWidth/2+(screenWidth /10) ){
                    camera.translate(-10,0);

                }
                if (playerSprite.getX()>camera.position.x-camera.viewportWidth/2+((screenWidth /20)*18)){
                    camera.translate(10,0);
                }
                if (playerSprite.getY()<camera.position.y-camera.viewportHeight/2+(screenHeight /10) ){
                    camera.translate(0,-10);
                }
                if (playerSprite.getY()>camera.position.y-camera.viewportHeight/2+((screenHeight /20)*18)){
                    camera.translate(0,10);
                }
            }
            else {
                camera.translate(-(screenX - oldx)*camera.zoom, (screenY - oldy)*camera.zoom);



            }
            limitCameraPanning();
        }
            else firsttouch = false;
        oldx = screenX;oldy = screenY;
        return false;
    }

    private void limitCameraPanning() {
        Vector3 translation = camera.position;
        translation.x = Math.max(screenWidth / 2 * camera.zoom, translation.x);
        translation.y = Math.max(screenHeight / 2 * camera.zoom, translation.y);
        translation.x = Math.min(tileWidth * tiledMapWidth - screenWidth / 2 * camera.zoom, translation.x);
        translation.y = Math.min(tileHeight * tiledMapHeight - screenHeight / 2 * camera.zoom, translation.y);
    }

    private boolean isPlayerBlocked(TiledMapTileLayer layer, int playerX, int playerY) {
        return isBlocked(layer, playerX, playerY) ||
                isBlocked(layer, playerX, (playerY + texture.getWidth())) ||
                isBlocked(layer, (playerX + texture.getWidth()), playerY) ||
                isBlocked(layer, (playerX + texture.getWidth()), playerY + texture.getHeight());
    }

    private Integer getTileWidth() {
        return tileWidth;
    }

    private boolean isBlocked(TiledMapTileLayer layer, int x, int y) {
        x/=tileHeight;
        y/=tileWidth;
        if (layer.getCell(x, y) == null) return true;
        return layer.getCell(x, y).getTile().getProperties().containsKey(BLOCKING_PROPERTY);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        camera.zoom *=  initialDistance / distance;
        camera.zoom = Math.max(camera.zoom, 1);
        camera.zoom = Math.min(camera.zoom, minZoom);
        Gdx.app.log("Zoom", "Zoomed to level " + camera.zoom);
        limitCameraPanning();
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }


}
