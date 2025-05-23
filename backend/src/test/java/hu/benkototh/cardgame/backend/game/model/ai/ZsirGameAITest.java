package hu.benkototh.cardgame.backend.game.model.ai;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZsirGameAITest {

    @Mock
    private ZsirGame mockGame;

    private Player aiPlayer;
    private Player humanPlayer;
    private ZsirGameAI zsirGameAI;
    private Deck mockDeck;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        aiPlayer = new Player();
        aiPlayer.setId("ai-123");
        aiPlayer.setUsername("AI Player");
        aiPlayer.setHand(new ArrayList<>());
        aiPlayer.setWonCards(new ArrayList<>());
        aiPlayer.setAI(true);

        humanPlayer = new Player();
        humanPlayer.setId("human-456");
        humanPlayer.setUsername("Human Player");
        humanPlayer.setHand(new ArrayList<>());
        humanPlayer.setWonCards(new ArrayList<>());

        mockDeck = new Deck();

        zsirGameAI = new ZsirGameAI(mockGame, aiPlayer, humanPlayer);
    }

    @Test
    void testDecideMove_EmptyTrick_ShouldLeadCard() {
        List<Card> emptyTrick = new ArrayList<>();
        when(mockGame.fetchCurrentTrickCards()).thenReturn(emptyTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(aiPlayer.getId());

        Card card1 = new Card(Suit.HEARTS, Rank.SEVEN);
        Card card2 = new Card(Suit.BELLS, Rank.KING);
        aiPlayer.getHand().add(card1);
        aiPlayer.getHand().add(card2);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        assertNotNull(action.getCardParameter("card"));
    }

    @Test
    void testDecideMove_CanHit_ShouldHitOrPass() {
        List<Card> currentTrick = new ArrayList<>();
        currentTrick.add(new Card(Suit.HEARTS, Rank.KING));
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(true);

        Card matchingCard = new Card(Suit.BELLS, Rank.KING);
        aiPlayer.getHand().add(matchingCard);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        if ("playCard".equals(action.getActionType())) {
            Card playedCard = action.getCardParameter("card");
            assertNotNull(playedCard);
            assertTrue(playedCard.getRank() == Rank.KING || playedCard.getRank() == Rank.SEVEN);
        } else {
            assertEquals("pass", action.getActionType());
        }
    }

    @Test
    void testDecideMove_CanHit_NoMatchingCards_ShouldPass() {
        List<Card> currentTrick = new ArrayList<>();
        currentTrick.add(new Card(Suit.HEARTS, Rank.KING));
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(true);

        Card nonMatchingCard = new Card(Suit.BELLS, Rank.NINE);
        aiPlayer.getHand().add(nonMatchingCard);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("pass", action.getActionType());
    }

    @Test
    void testDecideMove_ResponseCard_ShouldRespondToLead() {
        List<Card> currentTrick = new ArrayList<>();
        currentTrick.add(new Card(Suit.HEARTS, Rank.KING));
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(humanPlayer.getId());

        Card card1 = new Card(Suit.HEARTS, Rank.SEVEN);
        Card card2 = new Card(Suit.BELLS, Rank.NINE);
        aiPlayer.getHand().add(card1);
        aiPlayer.getHand().add(card2);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        assertNotNull(action.getCardParameter("card"));
    }

    @Test
    void testDecideMove_ValuableTrick_ShouldHit() {
        List<Card> currentTrick = new ArrayList<>();
        currentTrick.add(new Card(Suit.HEARTS, Rank.ACE));
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(true);

        Card matchingCard = new Card(Suit.BELLS, Rank.SEVEN);
        aiPlayer.getHand().add(matchingCard);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        Card playedCard = action.getCardParameter("card");
        assertNotNull(playedCard);
        assertEquals(Rank.SEVEN, playedCard.getRank());
    }

    @Test
    void testDecideMove_EmptyHand_ShouldHandleGracefully() {
        List<Card> currentTrick = new ArrayList<>();
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(aiPlayer.getId());

        aiPlayer.setHand(new ArrayList<>());

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("pass", action.getActionType());
    }

    @Test
    void testDecideMove_EndgameWithValuableCards_ShouldPlayStrategically() {
        List<Card> currentTrick = new ArrayList<>();
        currentTrick.add(new Card(Suit.HEARTS, Rank.NINE));
        when(mockGame.fetchCurrentTrickCards()).thenReturn(currentTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(humanPlayer.getId());

        when(mockGame.getGameState("deck")).thenReturn(new Deck());

        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card ten = new Card(Suit.BELLS, Rank.TEN);
        Card king = new Card(Suit.LEAVES, Rank.KING);
        aiPlayer.getHand().add(ace);
        aiPlayer.getHand().add(ten);
        aiPlayer.getHand().add(king);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        Card playedCard = action.getCardParameter("card");
        assertNotNull(playedCard);
        assertEquals(Rank.KING, playedCard.getRank());
    }

    @Test
    void testDecideMove_LeadWithNonValuableCard() {
        List<Card> emptyTrick = new ArrayList<>();
        when(mockGame.fetchCurrentTrickCards()).thenReturn(emptyTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(aiPlayer.getId());

        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card nine = new Card(Suit.BELLS, Rank.NINE);
        Card king = new Card(Suit.LEAVES, Rank.KING);
        aiPlayer.getHand().add(ace);
        aiPlayer.getHand().add(nine);
        aiPlayer.getHand().add(king);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        Card playedCard = action.getCardParameter("card");
        assertNotNull(playedCard);
        assertTrue(playedCard.getRank() == Rank.NINE || playedCard.getRank() == Rank.KING);
    }

    @Test
    void testDecideMove_OnlyValuableCardsInHand() {
        List<Card> emptyTrick = new ArrayList<>();
        when(mockGame.fetchCurrentTrickCards()).thenReturn(emptyTrick);
        when(mockGame.getGameState("canHit")).thenReturn(false);
        when(mockGame.getGameState("trickStarter")).thenReturn(aiPlayer.getId());

        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card ten = new Card(Suit.BELLS, Rank.TEN);
        aiPlayer.getHand().add(ace);
        aiPlayer.getHand().add(ten);

        GameAction action = zsirGameAI.decideMove();

        assertNotNull(action);
        assertEquals("playCard", action.getActionType());
        Card playedCard = action.getCardParameter("card");
        assertNotNull(playedCard);
        assertTrue(playedCard.getRank() == Rank.ACE || playedCard.getRank() == Rank.TEN);
    }
}
