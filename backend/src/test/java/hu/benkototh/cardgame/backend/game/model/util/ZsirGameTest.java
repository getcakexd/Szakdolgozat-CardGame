package hu.benkototh.cardgame.backend.game.model.util;

import hu.benkototh.cardgame.backend.game.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ZsirGameUtilsTest {

    @Test
    void testConvertToCard_WithCardObject() {
        Card originalCard = new Card(Suit.HEARTS, Rank.ACE);

        Card convertedCard = ZsirGameUtils.convertToCard(originalCard);

        assertNotNull(convertedCard);
        assertEquals(Suit.HEARTS, convertedCard.getSuit());
        assertEquals(Rank.ACE, convertedCard.getRank());
    }

    @Test
    void testConvertToCard_WithMapObject() {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("suit", "HEARTS");
        cardMap.put("rank", "ACE");

        Card convertedCard = ZsirGameUtils.convertToCard(cardMap);

        assertNotNull(convertedCard);
        assertEquals(Suit.HEARTS, convertedCard.getSuit());
        assertEquals(Rank.ACE, convertedCard.getRank());
    }

    @Test
    void testConvertToCard_WithInvalidMapObject() {
        Map<String, Object> invalidCardMap = new HashMap<>();
        invalidCardMap.put("suit", "INVALID_SUIT");
        invalidCardMap.put("rank", "ACE");

        Card convertedCard = ZsirGameUtils.convertToCard(invalidCardMap);

        assertNull(convertedCard);
    }

    @Test
    void testConvertToCard_WithNullObject() {
        Card convertedCard = ZsirGameUtils.convertToCard(null);

        assertNull(convertedCard);
    }

    @Test
    void testConvertToCardList_WithListOfCards() {
        List<Card> originalList = new ArrayList<>();
        originalList.add(new Card(Suit.HEARTS, Rank.ACE));
        originalList.add(new Card(Suit.BELLS, Rank.KING));

        List<Card> convertedList = ZsirGameUtils.convertToCardList(originalList);

        assertEquals(2, convertedList.size());
        assertEquals(Suit.HEARTS, convertedList.get(0).getSuit());
        assertEquals(Rank.ACE, convertedList.get(0).getRank());
        assertEquals(Suit.BELLS, convertedList.get(1).getSuit());
        assertEquals(Rank.KING, convertedList.get(1).getRank());
    }

    @Test
    void testConvertToCardList_WithListOfMaps() {
        List<Map<String, Object>> originalList = new ArrayList<>();

        Map<String, Object> card1Map = new HashMap<>();
        card1Map.put("suit", "HEARTS");
        card1Map.put("rank", "ACE");
        originalList.add(card1Map);

        Map<String, Object> card2Map = new HashMap<>();
        card2Map.put("suit", "BELLS");
        card2Map.put("rank", "KING");
        originalList.add(card2Map);

        List<Card> convertedList = ZsirGameUtils.convertToCardList(originalList);

        assertEquals(2, convertedList.size());
        assertEquals(Suit.HEARTS, convertedList.get(0).getSuit());
        assertEquals(Rank.ACE, convertedList.get(0).getRank());
        assertEquals(Suit.BELLS, convertedList.get(1).getSuit());
        assertEquals(Rank.KING, convertedList.get(1).getRank());
    }

    @Test
    void testConvertToCardList_WithMixedList() {
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(new Card(Suit.HEARTS, Rank.ACE));

        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("suit", "BELLS");
        cardMap.put("rank", "KING");
        mixedList.add(cardMap);

        mixedList.add("Not a card");

        List<Card> convertedList = ZsirGameUtils.convertToCardList(mixedList);

        assertEquals(2, convertedList.size());
        assertEquals(Suit.HEARTS, convertedList.get(0).getSuit());
        assertEquals(Rank.ACE, convertedList.get(0).getRank());
        assertEquals(Suit.BELLS, convertedList.get(1).getSuit());
        assertEquals(Rank.KING, convertedList.get(1).getRank());
    }

    @Test
    void testPlayerHasCard_CardInHand() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.KING));
        player.setHand(hand);

        Card cardToCheck = new Card(Suit.HEARTS, Rank.ACE);

        assertTrue(ZsirGameUtils.playerHasCard(player, cardToCheck));
    }

    @Test
    void testPlayerHasCard_CardNotInHand() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.KING));
        player.setHand(hand);

        Card cardToCheck = new Card(Suit.LEAVES, Rank.SEVEN);

        assertFalse(ZsirGameUtils.playerHasCard(player, cardToCheck));
    }

    @Test
    void testRemoveCardFromHand_CardInHand() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.KING));
        player.setHand(hand);

        Card cardToRemove = new Card(Suit.HEARTS, Rank.ACE);

        ZsirGameUtils.removeCardFromHand(player, cardToRemove);

        assertEquals(1, player.getHand().size());
        assertEquals(Suit.BELLS, player.getHand().get(0).getSuit());
        assertEquals(Rank.KING, player.getHand().get(0).getRank());
    }

    @Test
    void testRemoveCardFromHand_CardNotInHand() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.KING));
        player.setHand(hand);

        Card cardToRemove = new Card(Suit.LEAVES, Rank.SEVEN);

        ZsirGameUtils.removeCardFromHand(player, cardToRemove);

        assertEquals(2, player.getHand().size());
    }

    @Test
    void testCheckIfPlayerCanHit_CanHitWithMatchingRank() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.KING));
        player.setHand(hand);

        Card cardToMatch = new Card(Suit.LEAVES, Rank.KING);

        assertTrue(ZsirGameUtils.checkIfPlayerCanHit(player, cardToMatch));
    }

    @Test
    void testCheckIfPlayerCanHit_CanHitWithSeven() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.SEVEN));
        hand.add(new Card(Suit.BELLS, Rank.ACE));
        player.setHand(hand);

        Card cardToMatch = new Card(Suit.LEAVES, Rank.KING);

        assertTrue(ZsirGameUtils.checkIfPlayerCanHit(player, cardToMatch));
    }

    @Test
    void testCheckIfPlayerCanHit_CannotHit() {
        Player player = new Player();
        List<Card> hand = new ArrayList<>();
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.BELLS, Rank.NINE));
        player.setHand(hand);

        Card cardToMatch = new Card(Suit.LEAVES, Rank.KING);

        assertFalse(ZsirGameUtils.checkIfPlayerCanHit(player, cardToMatch));
    }

    @Test
    void testGetOtherPlayer_TwoPlayers() {
        List<Player> players = new ArrayList<>();

        Player player1 = new Player();
        player1.setId("1");
        players.add(player1);

        Player player2 = new Player();
        player2.setId("2");
        players.add(player2);

        assertEquals(player2, ZsirGameUtils.getOtherPlayer(players, player1));
        assertEquals(player1, ZsirGameUtils.getOtherPlayer(players, player2));
    }

    @Test
    void testGetOtherPlayer_NotTwoPlayers() {
        List<Player> players = new ArrayList<>();

        Player player1 = new Player();
        player1.setId("1");
        players.add(player1);

        assertEquals(player1, ZsirGameUtils.getOtherPlayer(players, player1));
    }

    @Test
    void testConvertToCardList_WithNullList() {
        List<Card> convertedList = ZsirGameUtils.convertToCardList(null);

        assertNotNull(convertedList);
        assertTrue(convertedList.isEmpty());
    }

    @Test
    void testConvertToCardList_WithEmptyList() {
        List<Object> emptyList = new ArrayList<>();

        List<Card> convertedList = ZsirGameUtils.convertToCardList(emptyList);

        assertNotNull(convertedList);
        assertTrue(convertedList.isEmpty());
    }

    @Test
    void testConvertToCardList_WithInvalidMaps() {
        List<Map<String, Object>> originalList = new ArrayList<>();

        Map<String, Object> invalidCard1 = new HashMap<>();
        invalidCard1.put("suit", "INVALID_SUIT");
        invalidCard1.put("rank", "ACE");
        originalList.add(invalidCard1);

        Map<String, Object> invalidCard2 = new HashMap<>();
        invalidCard2.put("suit", "HEARTS");
        invalidCard2.put("rank", "INVALID_RANK");
        originalList.add(invalidCard2);

        List<Card> convertedList = ZsirGameUtils.convertToCardList(originalList);

        assertTrue(convertedList.isEmpty());
    }

    @Test
    void testConvertToCardList_WithMissingFields() {
        List<Map<String, Object>> originalList = new ArrayList<>();

        Map<String, Object> missingRank = new HashMap<>();
        missingRank.put("suit", "HEARTS");
        originalList.add(missingRank);

        Map<String, Object> missingSuit = new HashMap<>();
        missingSuit.put("rank", "ACE");
        originalList.add(missingSuit);

        List<Card> convertedList = ZsirGameUtils.convertToCardList(originalList);

        assertTrue(convertedList.isEmpty());
    }

    @Test
    void testPlayerHasCard_EmptyHand() {
        Player player = new Player();
        player.setHand(new ArrayList<>());

        Card cardToCheck = new Card(Suit.HEARTS, Rank.ACE);

        assertFalse(ZsirGameUtils.playerHasCard(player, cardToCheck));
    }


    @Test
    void testRemoveCardFromHand_EmptyHand() {
        Player player = new Player();
        player.setHand(new ArrayList<>());

        Card cardToRemove = new Card(Suit.HEARTS, Rank.ACE);

        ZsirGameUtils.removeCardFromHand(player, cardToRemove);

        assertTrue(player.getHand().isEmpty());
    }
    
    @Test
    void testCheckIfPlayerCanHit_EmptyHand() {
        Player player = new Player();
        player.setHand(new ArrayList<>());

        Card cardToMatch = new Card(Suit.HEARTS, Rank.ACE);

        assertFalse(ZsirGameUtils.checkIfPlayerCanHit(player, cardToMatch));
    }

    @Test
    void testGetOtherPlayer_ThreePlayers() {
        List<Player> players = new ArrayList<>();

        Player player1 = new Player();
        player1.setId("1");
        players.add(player1);

        Player player2 = new Player();
        player2.setId("2");
        players.add(player2);

        Player player3 = new Player();
        player3.setId("3");
        players.add(player3);

        assertEquals(player1, ZsirGameUtils.getOtherPlayer(players, player1));
    }

    @Test
    void testConvertToCard_WithNonMapNonCardObject() {
        String notACard = "This is not a card";

        Card convertedCard = ZsirGameUtils.convertToCard(notACard);

        assertNull(convertedCard);
    }
}
