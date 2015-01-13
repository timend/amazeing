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
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor, GestureDetector.GestureListener {
    public static final String BLOCKING_PROPERTY = "wall";
    public static final String GOAL_PROPERTY = "goal";
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
    TiledMapTile[] wallTiles,fogOfWarTiles;
    StaticTiledMapTile[] goalTiles;
    int distanceToGoal;
    float zoomSensitivity = 0.5f;
    StaticTiledMapTile empty;
    AnimatedTiledMapTile animatedGoalTile;
    long timeAtMazeChangeInMillis;
    boolean mazeChangeing;
    TiledMapTileLayer fogOfWarLayer;
    MazeGenerator mazeGenerator;
    @Override
    public void create () {
        mazeChangeing = false;
        timeAtMazeChangeInMillis = 0;
        distanceToGoal = 200;
        tileWidth=64;
        tileHeight=64;
        tiledMapHeight = 51;
        tiledMapWidth = 51;
        playerVelocity = 0.2f;
        wallTiles = new TiledMapTile[16];
        fogOfWarTiles = new TiledMapTile[16];
        fogOfWarLayer = new TiledMapTileLayer(tiledMapWidth,tiledMapHeight,tileWidth,tileHeight);
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

        for (int i = 0; i < 4; i++) {
            for (int j = 4; j < 8; j++) {
                TiledMapTile tile = new StaticTiledMapTile(splitTiles[i][j]);
                fogOfWarTiles[rowDirections[i]|colDirections[j-4]] = tile;
            }
        }
        for (int i = 0; i <= tiledMapWidth; i++) {
            for (int j = 0; j < tiledMapHeight; j++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(fogOfWarTiles[0]);
                fogOfWarLayer.setCell(i,j,cell);
            }
        }
        int k = 0;
        goalTiles = new StaticTiledMapTile[24];
        for (int j = 4; j < 7; j++) {
            for (int i = 0; i < 4; i++) {
                goalTiles[k] = new StaticTiledMapTile(splitTiles[j][i]);
                goalTiles[23-k] = new StaticTiledMapTile(splitTiles[j][i]);
                k++;
            }
        }
        Array<StaticTiledMapTile> goalTilesArray = new Array<StaticTiledMapTile>(goalTiles);
        animatedGoalTile = new AnimatedTiledMapTile(1f/12f,goalTilesArray);
        animatedGoalTile.getProperties().put(GOAL_PROPERTY,true);
        empty = new StaticTiledMapTile(splitTiles[0][0]);
        empty.setId(1);
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();
        //tiledMap = new TmxMapLoader().load("TilesetTest2.tmx");
        MazeGenerator mazeGenerator = new MazeGenerator(tiledMapWidth,tiledMapHeight,empty,wallTiles, animatedGoalTile,distanceToGoal);
        tiledMap = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(tiledMapWidth,tiledMapHeight,tileWidth,tileHeight);
        mazeGenerator.fillMapLayer(layer);
        tiledMap.getLayers().add(layer);
        tiledMap.getLayers().add(fogOfWarLayer);

        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(tiledMap);
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(null);
        fogOfWarLayer.setCell(1, 1, cell);
        fogOfWarLayer.setCell((int)mazeGenerator.getGoalPosition().x,(int)mazeGenerator.getGoalPosition().y,cell);
        GestureDetector gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(gestureDetector, this));
        tiledMap.getLayers().get(0).setVisible(true);
        minZoom = Math.max(((float) getTileWidth() * tiledMapWidth / (float) screenWidth),((float) tileHeight * tiledMapHeight / (float) screenHeight));
        texture = new Texture(Gdx.files.internal("PlayerSprite.png"));
        playerSprite = new Sprite(texture);
        tiledMapRenderer.addSprite(playerSprite);
        //MapProperties startPos = tiledMap.getLayers().get(1).getObjects().get("startPos").getProperties();
        playerSprite.setPosition(/*(Float)startPos.get("x")*/ tileWidth  ,/*(Float)startPos.get("y")*/ tileHeight );
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        if (mazeChangeing&&inNewMazeDelay()) {
            Gdx.gl.glClearColor(0, 0, 0, (System.currentTimeMillis() - timeAtMazeChangeInMillis) / 5);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
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
                refreshFogOfWar(playerX/tileWidth,playerY/tileHeight);
                int moveRoundCounter;
                moveRoundCounter = 0;
                while ((playerX != playerDestX || playerY != playerDestY) &&
                        !(moveRoundCounter > playerVelocity / Gdx.graphics.getDeltaTime())&& !inNewMazeDelay()) {
                    if (mazeChangeing) {
                        mazeChangeing = false;
                    }

                    if(layer.getCell(playerX/tileWidth, playerY/tileHeight).getTile().getProperties().containsKey(GOAL_PROPERTY))
                    {
                        mazeChangeing = true;
                        timeAtMazeChangeInMillis = System.currentTimeMillis();
                        MazeGenerator mazeGenerator = new MazeGenerator(tiledMapWidth,tiledMapHeight,empty,wallTiles, animatedGoalTile,distanceToGoal);
                        tiledMap = new TiledMap();
                        layer = new TiledMapTileLayer(tiledMapWidth,tiledMapHeight,tileWidth,tileHeight);
                        mazeGenerator.fillMapLayer(layer);
                        for (int i = 0; i <= tiledMapWidth; i++) {
                            for (int j = 0; j < tiledMapHeight; j++) {
                                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                                cell.setTile(fogOfWarTiles[0]);
                                fogOfWarLayer.setCell(i,j,cell);
                            }
                        }
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(null);
                        fogOfWarLayer.setCell((int) mazeGenerator.getGoalPosition().x, (int) mazeGenerator.getGoalPosition().y, cell);
                        fogOfWarLayer.setCell(1,1,cell);
                        tiledMap.getLayers().add(layer);
                        tiledMap.getLayers().add(fogOfWarLayer);
                        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(tiledMap);
                        playerSprite = new Sprite(texture);
                        tiledMapRenderer.addSprite(playerSprite);
                        playerSprite.setX(tileWidth);
                        playerSprite.setY(tileHeight);

                        camera.translate(-camera.position.x,-camera.position.y);
                        camera.translate(screenWidth/2,screenHeight/2);
                        break;
                    }
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
                if (!mazeChangeing)
                {
                    playerSprite.setPosition(playerX, playerY);
                }
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
        float minX = screenWidth / 2 * camera.zoom;
        float minY = screenHeight / 2 * camera.zoom;
        float maxX = tileWidth * tiledMapWidth - screenWidth / 2 * camera.zoom;
        float maxY = tileHeight * tiledMapHeight - screenHeight / 2 * camera.zoom;

        if (minX >= maxX)
        {
            translation.x = tileWidth * tiledMapWidth / 2;
        }
        else
        {
            translation.x = Math.max(minX, translation.x);
            translation.x = Math.min(maxX, translation.x);
        }

        if (minY >= maxY)
        {
            translation.y = tileHeight * tiledMapHeight / 2;
        }
        else
        {
            translation.y = Math.max(minY, translation.y);
            translation.y = Math.min(maxY, translation.y);
        }
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

    private boolean inNewMazeDelay()
    {
        if (mazeChangeing)
        {
                return (System.currentTimeMillis() < timeAtMazeChangeInMillis + 1000);
        }
        return false;
    }

    private void refreshFogOfWar(int playerPositionInTilesX, int playerPositionInTilesY)
    {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        for (int i = playerPositionInTilesX - 1;i<=playerPositionInTilesX +1;i++){
            for (int j = playerPositionInTilesY - 1; j <= playerPositionInTilesY + 1; j++) {
                cell.setTile(null);
                fogOfWarLayer.setCell(i,j,cell);

            }
        }
        for (int i = playerPositionInTilesX - 2; i <= playerPositionInTilesX + 2; i++) {
            if (isFog(i,playerPositionInTilesY - 2)) refreshFogTile(i,playerPositionInTilesY - 2);
            if (isFog(i,playerPositionInTilesY + 2))refreshFogTile(i,playerPositionInTilesY + 2);
        }
        for (int i = playerPositionInTilesY - 2; i <= playerPositionInTilesY +2; i++) {
            if (isFog(playerPositionInTilesX - 2,i))refreshFogTile(playerPositionInTilesX - 2,i);
            if (isFog(playerPositionInTilesX + 2,i))refreshFogTile(playerPositionInTilesX + 2,i);
        }
    }

    private void refreshFogTile(int positionX,int positionY)
    {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        int fogTileIndex = 0;
        for(Direction dir:Direction.values()) {

            if (mazeGenerator.between(positionX + dir.dx, tiledMapWidth) && mazeGenerator.between(positionY + dir.dy, tiledMapHeight) && isFog(positionX+dir.dx,positionY+dir.dy)){
                fogTileIndex|=dir.bit;
            }
        }

        cell.setTile(fogOfWarTiles[15-fogTileIndex]);
        fogOfWarLayer.setCell(positionX,positionY,cell);

    }

    private boolean isFog(int x, int y)
    {
        if (mazeGenerator.between(x,tiledMapWidth)&&mazeGenerator.between(y,tiledMapHeight)) {
            TiledMapTileLayer.Cell cell = fogOfWarLayer.getCell(x, y);
            return (cell != null && cell.getTile() != null);
        }
        else return true;

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
        camera.position.set(playerSprite.getX(), playerSprite.getY(), 0);
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
