package hu.benkototh.cardgame.backend.game.model.game;

import hu.benkototh.cardgame.backend.game.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZsirGameTest {

    private ZsirGame zsirGame;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        zsirGame = new ZsirGame(2);
        zsirGame.setGameDefinitionId(1L);
        zsirGame.setName("Test Zsir Game");

        player1 = new Player();
        player1.setId("1");
        player1.setUsername("Player 1");
        player1.setHand(new ArrayList<>());
        player1.setWonCards(new ArrayList<>());
        player1.setGame(zsirGame);

        player2 = new Player();
        player2.setId("2");
        player2.setUsername("Player 2");
        player2.setHand(new ArrayList<>());
        player2.setWonCards(new ArrayList<>());
        player2.setGame(zsirGame);

        zsirGame.getPlayers().add(player1);
        zsirGame.getPlayers().add(player2);
    }

    @Test
    void testInitializeGame() {
        zsirGame.initializeGame();

        assertEquals(4, player1.getHand().size());
        assertEquals(4, player2.getHand().size());
        assertNotNull(zsirGame.getGameState("deck"));
        assertNotNull(zsirGame.getGameState("currentTrick"));
        assertEquals(0, ((List<?>) zsirGame.getGameState("currentTrick")).size());
        assertNotNull(zsirGame.getCurrentPlayer());
    }

    @Test
    void testIsValidMove_ValidPlayCard() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        Card card = player1.getHand().get(0);
        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);

        assertTrue(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testIsValidMove_NotPlayersTurn() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        Card card = player2.getHand().get(0);
        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);

        assertFalse(zsirGame.isValidMove(player2.getId(), action));
    }

    @Test
    void testIsValidMove_InvalidActionType() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        GameAction action = new GameAction();
        action.setActionType("invalidAction");

        assertFalse(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testIsValidMove_CardNotInHand() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        Card card = new Card(Suit.HEARTS, Rank.ACE);
        player1.getHand().removeIf(c -> c.getSuit() == Suit.HEARTS && c.getRank() == Rank.ACE);

        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);

        assertFalse(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testExecuteMove_PlayCard() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        Card card = player1.getHand().get(0);
        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);

        int initialHandSize = player1.getHand().size();

        zsirGame.executeMove(player1.getId(), action);

        assertEquals(initialHandSize - 1, player1.getHand().size());
        List<Card> currentTrick = zsirGame.fetchCurrentTrickCards();
        assertEquals(1, currentTrick.size());
        assertEquals(card.getSuit(), currentTrick.get(0).getSuit());
        assertEquals(card.getRank(), currentTrick.get(0).getRank());
        assertEquals(player2.getId(), zsirGame.getCurrentPlayer().getId());
    }

    @Test
    void testExecuteMove_CompleteTrick() {
        zsirGame.initializeGame();

        zsirGame.setCurrentPlayer(player1);
        Card card1 = player1.getHand().get(0);
        GameAction action1 = new GameAction();
        action1.setActionType("playCard");
        action1.addParameter("card", card1);
        zsirGame.executeMove(player1.getId(), action1);

        Card card2 = player2.getHand().get(0);
        while (card2.getRank() == Rank.SEVEN || card2.getRank() == card1.getRank()) {
            card2 = new Card(Suit.BELLS, Rank.NINE);
        }
        player2.getHand().add(card2);

        GameAction action2 = new GameAction();
        action2.setActionType("playCard");
        action2.addParameter("card", card2);

        zsirGame.executeMove(player2.getId(), action2);

        List<Card> currentTrick = zsirGame.fetchCurrentTrickCards();
        assertEquals(0, currentTrick.size());

        List<Card> wonCards = player1.getWonCards();
        assertEquals(2, wonCards.size());
        assertTrue(wonCards.stream().anyMatch(c -> c.getSuit() == card1.getSuit() && c.getRank() == card1.getRank()));
        Card finalCard = card2;
        assertTrue(wonCards.stream().anyMatch(c -> c.getSuit() == finalCard.getSuit() && c.getRank() == finalCard.getRank()));

        assertEquals(player1.getId(), zsirGame.getCurrentPlayer().getId());
    }

    @Test
    void testDetermineTrickWinner_FirstPlayerWins() {
        List<Card> currentTrick = new ArrayList<>();
        Card card1 = new Card(Suit.HEARTS, Rank.KING);
        Card card2 = new Card(Suit.BELLS, Rank.NINE);
        currentTrick.add(card1);
        currentTrick.add(card2);

        Player winner = zsirGame.determineTrickWinner(currentTrick, player1.getId());

        assertEquals(player1.getId(), winner.getId());
    }

    @Test
    void testDetermineTrickWinner_SecondPlayerWinsWithMatchingCard() {
        List<Card> currentTrick = new ArrayList<>();
        Card card1 = new Card(Suit.HEARTS, Rank.KING);
        Card card2 = new Card(Suit.BELLS, Rank.KING);
        currentTrick.add(card1);
        currentTrick.add(card2);

        Player winner = zsirGame.determineTrickWinner(currentTrick, player1.getId());

        assertEquals(player2.getId(), winner.getId());
    }

    @Test
    void testDetermineTrickWinner_SecondPlayerWinsWithSeven() {
        List<Card> currentTrick = new ArrayList<>();
        Card card1 = new Card(Suit.HEARTS, Rank.KING);
        Card card2 = new Card(Suit.BELLS, Rank.SEVEN);
        currentTrick.add(card1);
        currentTrick.add(card2);

        Player winner = zsirGame.determineTrickWinner(currentTrick, player1.getId());

        assertEquals(player2.getId(), winner.getId());
    }

    @Test
    void testIsGameOver_NotOver() {
        zsirGame.initializeGame();

        assertFalse(zsirGame.isGameOver());
    }

    @Test
    void testIsGameOver_GameOver() {
        zsirGame.initializeGame();

        zsirGame.setGameState("deck", new Deck());
        player1.setHand(new ArrayList<>());
        player2.setHand(new ArrayList<>());
        zsirGame.setGameState("currentTrick", new ArrayList<>());

        assertTrue(zsirGame.isGameOver());
    }

    @Test
    void testCalculateScores() {
        zsirGame.initializeGame();

        player1.getWonCards().add(new Card(Suit.HEARTS, Rank.ACE));
        player1.getWonCards().add(new Card(Suit.BELLS, Rank.TEN));

        player2.getWonCards().add(new Card(Suit.LEAVES, Rank.ACE));
        player2.getWonCards().add(new Card(Suit.ACORNS, Rank.KING));

        Map<String, Integer> scores = zsirGame.calculateScores();

        assertEquals(20, scores.get(player1.getId()));
        assertEquals(10, scores.get(player2.getId()));
        assertEquals(20, player1.getScore());
        assertEquals(10, player2.getScore());
    }

    @Test
    void testGetMinPlayers() {
        assertEquals(2, zsirGame.getMinPlayers());

        ZsirGame singlePlayerGame = new ZsirGame(1);
        assertEquals(1, singlePlayerGame.getMinPlayers());
    }

    @Test
    void testGetMaxPlayers() {
        assertEquals(2, zsirGame.getMaxPlayers());

        ZsirGame singlePlayerGame = new ZsirGame(1);
        assertEquals(1, singlePlayerGame.getMaxPlayers());
    }

    @Test
    void testHandlePass() {
        zsirGame.initializeGame();

        zsirGame.setCurrentPlayer(player1);
        Card card1 = player1.getHand().get(0);
        GameAction action1 = new GameAction();
        action1.setActionType("playCard");
        action1.addParameter("card", card1);
        zsirGame.executeMove(player1.getId(), action1);

        Card card2 = new Card(Suit.BELLS, Rank.SEVEN);
        player2.getHand().add(card2);
        GameAction action2 = new GameAction();
        action2.setActionType("playCard");
        action2.addParameter("card", card2);
        zsirGame.executeMove(player2.getId(), action2);

        GameAction passAction = new GameAction();
        passAction.setActionType("pass");

        zsirGame.handlePass(player1.getId());

        List<Card> currentTrick = zsirGame.fetchCurrentTrickCards();
        assertEquals(0, currentTrick.size());

        List<Card> wonCards = player2.getWonCards();
        assertEquals(2, wonCards.size());

        assertEquals(player2.getId(), zsirGame.getCurrentPlayer().getId());
    }

    @Test
    void testIsValidMove_ValidPass() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);
        zsirGame.setGameState("canHit", true);

        GameAction action = new GameAction();
        action.setActionType("pass");

        assertTrue(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testIsValidMove_InvalidPass_NotCanHit() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);
        zsirGame.setGameState("canHit", false);

        GameAction action = new GameAction();
        action.setActionType("pass");

        assertFalse(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testIsValidMove_InvalidPass_NotPlayersTurn() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);
        zsirGame.setGameState("canHit", true);

        GameAction action = new GameAction();
        action.setActionType("pass");

        assertFalse(zsirGame.isValidMove(player2.getId(), action));
    }

    @Test
    void testIsValidMove_NullAction() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        assertFalse(zsirGame.isValidMove(player1.getId(), null));
    }

    @Test
    void testIsValidMove_NullCard() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        GameAction action = new GameAction();
        action.setActionType("playCard");

        assertFalse(zsirGame.isValidMove(player1.getId(), action));
    }

    @Test
    void testExecuteMove_MaxCardsInTrick() {
        zsirGame.initializeGame();
        zsirGame.setCurrentPlayer(player1);

        List<Card> currentTrick = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            currentTrick.add(new Card(Suit.HEARTS, Rank.SEVEN));
        }
        zsirGame.setGameState("currentTrick", currentTrick);
        zsirGame.setGameState("trickStarter", player1.getId());

        Card card = new Card(Suit.BELLS, Rank.NINE);
        player1.getHand().add(card);

        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);

        zsirGame.executeMove(player1.getId(), action);

        List<Card> newTrick = zsirGame.fetchCurrentTrickCards();
        assertEquals(0, newTrick.size());

        List<Card> wonCards = player1.getWonCards();
        assertEquals(8, wonCards.size());
    }

    @Test
    void testCheckAndDrawCards() {
        zsirGame.initializeGame();

        player1.setHand(new ArrayList<>());
        player2.setHand(new ArrayList<>());

        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        zsirGame.setGameState("deck", deck);

        zsirGame.checkAndDrawCards();

        assertEquals(4, player1.getHand().size());
        assertEquals(4, player2.getHand().size());
    }

    @Test
    void testCheckAndDrawCards_EmptyDeck() {
        zsirGame.initializeGame();

        player1.setHand(new ArrayList<>());
        player2.setHand(new ArrayList<>());

        Deck emptyDeck = new Deck();
        zsirGame.setGameState("deck", emptyDeck);

        zsirGame.checkAndDrawCards();

        assertEquals(0, player1.getHand().size());
        assertEquals(0, player2.getHand().size());
    }

    @Test
    void testCheckAndDrawCards_PartialDeck() {
        zsirGame.initializeGame();

        player1.setHand(new ArrayList<>());
        player2.setHand(new ArrayList<>());

        Deck partialDeck = new Deck();
        for (int i = 0; i < 5; i++) {
            partialDeck.addCard(new Card(Suit.HEARTS, Rank.values()[i]));
        }
        zsirGame.setGameState("deck", partialDeck);

        zsirGame.checkAndDrawCards();

        assertEquals(4, player1.getHand().size());
        assertEquals(1, player2.getHand().size());

        Deck resultDeck = (Deck) zsirGame.getGameState("deck");
        assertTrue(resultDeck.isEmpty());
    }

    @Test
    void testRecordAbandonedUser() {
        zsirGame.initializeGame();

        zsirGame.recordAbandonedUser("1");
        zsirGame.recordAbandonedUser("2");
        zsirGame.recordAbandonedUser("1");

        List<String> abandonedUsers = zsirGame.fetchAbandonedUsers();
        assertEquals(2, abandonedUsers.size());
        assertTrue(abandonedUsers.contains("1"));
        assertTrue(abandonedUsers.contains("2"));
    }

    @Test
    void testFetchAbandonedUsers_NoAbandoned() {
        zsirGame.initializeGame();

        List<String> abandonedUsers = zsirGame.fetchAbandonedUsers();

        assertTrue(abandonedUsers.isEmpty());
    }

    @Test
    void testGetPlayerById() {
        zsirGame.initializeGame();

        assertEquals(player1, zsirGame.getPlayerById("1"));
        assertEquals(player2, zsirGame.getPlayerById("2"));
        assertNull(zsirGame.getPlayerById("nonexistent"));
    }

    @Test
    void testAddScore() {
        zsirGame.initializeGame();
        player1.setScore(0);

        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Suit.HEARTS, Rank.ACE));
        cards.add(new Card(Suit.BELLS, Rank.TEN));
        cards.add(new Card(Suit.LEAVES, Rank.KING));

        zsirGame.addScore(player1, cards);

        assertEquals(20, player1.getScore());
    }
}
