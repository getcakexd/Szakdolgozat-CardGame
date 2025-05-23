package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CardGameTest {

    private TestCardGame cardGame;
    private Player player1;
    private Player player2;

    private static class TestCardGame extends CardGame {
        private int minPlayers = 2;
        private int maxPlayers = 4;
        private boolean initializeGameCalled = false;
        private boolean isValidMoveCalled = false;
        private boolean executeMoveCalled = false;
        private boolean isGameOverCalled = false;
        private boolean calculateScoresCalled = false;

        @Override
        public void initializeGame() {
            initializeGameCalled = true;
        }

        @Override
        public boolean isValidMove(String playerId, GameAction action) {
            isValidMoveCalled = true;
            return true;
        }

        @Override
        public void executeMove(String playerId, GameAction action) {
            executeMoveCalled = true;
        }

        @Override
        public boolean isGameOver() {
            isGameOverCalled = true;
            return false;
        }

        @Override
        public Map<String, Integer> calculateScores() {
            calculateScoresCalled = true;
            Map<String, Integer> scores = new HashMap<>();
            for (Player player : getPlayers()) {
                scores.put(player.getId(), player.getScore());
            }
            return scores;
        }

        @Override
        public int getMinPlayers() {
            return minPlayers;
        }

        @Override
        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void setMinPlayers(int minPlayers) {
            this.minPlayers = minPlayers;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }
    }

    @BeforeEach
    void setUp() {
        cardGame = new TestCardGame();
        cardGame.setGameDefinitionId(1L);
        cardGame.setName("Test Card Game");
        
        player1 = new Player();
        player1.setId("1");
        player1.setUsername("Player 1");
        player1.setHand(new ArrayList<>());
        player1.setWonCards(new ArrayList<>());
        player1.setGame(cardGame);
        
        player2 = new Player();
        player2.setId("2");
        player2.setUsername("Player 2");
        player2.setHand(new ArrayList<>());
        player2.setWonCards(new ArrayList<>());
        player2.setGame(cardGame);
    }

    @Test
    void testAddPlayer_Success() {
        cardGame.addPlayer(player1);

        assertEquals(1, cardGame.getPlayers().size());
        assertEquals(player1, cardGame.getPlayers().get(0));
    }

    @Test
    void testAddPlayer_GameFull() {
        cardGame.setMaxPlayers(1);
        cardGame.addPlayer(player1);

        assertThrows(RuntimeException.class, () -> cardGame.addPlayer(player2));
    }

    @Test
    void testAddPlayer_GameNotWaiting() {
        cardGame.setStatus(GameStatus.ACTIVE);

        assertThrows(RuntimeException.class, () -> cardGame.addPlayer(player1));
    }

    @Test
    void testRemovePlayer_Success() {
        cardGame.addPlayer(player1);
        cardGame.addPlayer(player2);
        cardGame.setCurrentPlayer(player1);

        cardGame.removePlayer(player1.getId());

        assertEquals(1, cardGame.getPlayers().size());
        assertEquals(player2, cardGame.getPlayers().get(0));
        assertNull(cardGame.getCurrentPlayer());
    }

    @Test
    void testRemovePlayer_NotCurrentPlayer() {
        cardGame.addPlayer(player1);
        cardGame.addPlayer(player2);
        cardGame.setCurrentPlayer(player1);

        cardGame.removePlayer(player2.getId());
        
        assertEquals(1, cardGame.getPlayers().size());
        assertEquals(player1, cardGame.getPlayers().get(0));
        assertEquals(player1, cardGame.getCurrentPlayer());
    }

    @Test
    void testAddAbandonedUser() {
        cardGame.addAbandonedUser("1");
        cardGame.addAbandonedUser("2");
        cardGame.addAbandonedUser("1");

        List<String> abandonedUsers = cardGame.fetchAbandonedUsers();
        assertEquals(2, abandonedUsers.size());
        assertTrue(abandonedUsers.contains("1"));
        assertTrue(abandonedUsers.contains("2"));
    }

    @Test
    void testHasUserAbandoned() {
        cardGame.addAbandonedUser("1");
        
        assertTrue(cardGame.hasUserAbandoned("1"));
        assertFalse(cardGame.hasUserAbandoned("2"));
    }

    @Test
    void testStartGame_Success() {
        cardGame.addPlayer(player1);
        cardGame.addPlayer(player2);
        
        cardGame.startGame();
        
        assertEquals(GameStatus.ACTIVE, cardGame.getStatus());
        assertNotNull(cardGame.getStartedAt());
        assertTrue(((TestCardGame) cardGame).initializeGameCalled);
    }

    @Test
    void testStartGame_NotEnoughPlayers() {
        cardGame.setMinPlayers(3);
        cardGame.addPlayer(player1);
        cardGame.addPlayer(player2);
        
        assertThrows(RuntimeException.class, () -> cardGame.startGame());
        assertEquals(GameStatus.WAITING, cardGame.getStatus());
    }

    @Test
    void testStartGame_NotWaiting() {
        cardGame.addPlayer(player1);
        cardGame.addPlayer(player2);
        cardGame.setStatus(GameStatus.ACTIVE);
        
        assertThrows(RuntimeException.class, () -> cardGame.startGame());
    }

    @Test
    void testEndGame() {
        cardGame.endGame();
        
        assertEquals(GameStatus.FINISHED, cardGame.getStatus());
        assertNotNull(cardGame.getEndedAt());
    }

    @Test
    void testGameStateManagement() {
        cardGame.setGameState("testKey", "testValue");
        assertEquals("testValue", cardGame.getGameState("testKey"));
        
        assertTrue(cardGame.hasGameState("testKey"));
        assertFalse(cardGame.hasGameState("nonExistentKey"));
        
        cardGame.removeGameState("testKey");
        assertFalse(cardGame.hasGameState("testKey"));
        
        cardGame.setGameState("testKey", "testValue");
        cardGame.setGameState("testKey", null);
        assertFalse(cardGame.hasGameState("testKey"));
        
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        testList.add("item2");
        cardGame.setGameState("testList", testList);
        
        @SuppressWarnings("unchecked")
        List<String> retrievedList = (List<String>) cardGame.getGameState("testList");
        assertEquals(2, retrievedList.size());
        assertEquals("item1", retrievedList.get(0));
        
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", 123);
        cardGame.setGameState("testMap", testMap);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> retrievedMap = (Map<String, Object>) cardGame.getGameState("testMap");
        assertEquals(2, retrievedMap.size());
        assertEquals("value1", retrievedMap.get("key1"));
        assertEquals(123, retrievedMap.get("key2"));
    }

    @Test
    void testSerializeAndDeserializeGameState() {
        cardGame.setGameState("stringKey", "stringValue");
        cardGame.setGameState("intKey", 123);
        
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        testList.add("item2");
        cardGame.setGameState("listKey", testList);
        
        cardGame.serializeGameState();
        
        cardGame.getGameState().clear();
        
        cardGame.deserializeGameState();
        
        assertEquals("stringValue", cardGame.getGameState("stringKey"));
        assertEquals(123, cardGame.getGameState("intKey"));
        
        @SuppressWarnings("unchecked")
        List<String> deserializedList = (List<String>) cardGame.getGameState("listKey");
        assertEquals(2, deserializedList.size());
        assertEquals("item1", deserializedList.get(0));
        assertEquals("item2", deserializedList.get(1));
    }

    @Test
    void testProcessCommonGameStateObjects() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();

        cardGame.setGameState("deck", deck);
        cardGame.serializeGameState();
        
        cardGame.deserializeGameState();

        Object deserializedDeck = cardGame.getGameState("deck");
        assertInstanceOf(Deck.class, deserializedDeck);
        assertEquals(32, ((Deck) deserializedDeck).getCards().size());
    }
}
