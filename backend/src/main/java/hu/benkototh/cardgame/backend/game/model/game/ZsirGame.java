package hu.benkototh.cardgame.backend.game.model.game;

import hu.benkototh.cardgame.backend.game.model.ai.ZsirGameAI;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.model.util.ZsirGameUtils;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("ZSIR")
public class ZsirGame extends CardGame {
    private static final Logger logger = LoggerFactory.getLogger(ZsirGame.class);
    private static final int CARDS_PER_PLAYER = 4;
    private static final String CURRENT_TRICK = "currentTrick";
    private static final String TRICK_STARTER = "trickStarter";
    private static final String DECK = "deck";
    private static final String CAN_HIT = "canHit";
    private static final String LAST_PLAYED_CARD = "lastPlayedCard";
    private static final String LAST_PLAYER = "lastPlayer";
    private static final String AI_PLAYER = "aiPlayer";
    private static final String HUMAN_PLAYER = "humanPlayer";
    private static final int MAX_CARDS_IN_TRICK = 8;
    private static final String ABANDONED_USERS = "abandonedUsers";

    private int factoryId = 0;

    public ZsirGame(int factoryId) {
        if (factoryId == 0) {
            this.factoryId = (int) getGameState("FACTORY_ID");
        } else {
            setGameState("FACTORY_ID", factoryId);
            this.factoryId = factoryId;
        }
    }

    public ZsirGame() {
    }

    @Override
    public void initializeGame() {
        logger.debug("Initializing Zsir game with factoryId: {}", factoryId);
        switch (factoryId){
            case 1:
                initializeGame_Factory_1();
                break;
            case 2:
                initializeGame_Factory_2();
                break;
            case 4:
                initializeGame_Factory_4();
                break;
            default:
                defaultSwithThrow();
        }
    }

    @Override
    protected void processGameStateObjects() {
        logger.debug("Processing game state objects for Zsir game with factoryId: {}", factoryId);
        switch (factoryId){
            case 1:
                processGameStateObjects_1();
                break;
            case 2:
                processGameStateObjects_2();
                break;
            case 4:
                processGameStateObjects_4();
                break;
            default:
                defaultSwithThrow();
        }
    }

    @Override
    public boolean isValidMove(String playerId, GameAction action) {
        logger.debug("Validating move for playerId: {} with factoryId: {}", playerId, factoryId);
        switch (factoryId){
            case 1:
                return isValidMove_1(playerId, action);
            case 2:
                return isValidMove_2(playerId, action);
            case 4:
                return isValidMove_4(playerId, action);
            default:
                defaultSwithThrow();
        }
        return false;
    }

    @Override
    public void executeMove(String playerId, GameAction action) {
        logger.debug("Executing move for playerId: {} with factoryId: {}", playerId, factoryId);
        switch (factoryId){
            case 1:
                executeMove_1(playerId, action);
                break;
            case 2:
                executeMove_2(playerId, action);
                break;
            case 4:
                executeMove_4(playerId, action);
                break;
            default:
                defaultSwithThrow();
        }
    }

    public void initializeGame_Factory_1(){
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        deck.shuffle();

        if (getPlayers().size() != 1) {
            logger.error("Factory 1 requires exactly 1 human player");
            throw new IllegalStateException("Factory 1 requires exactly 1 human player");
        }

        Player humanPlayer = getPlayers().get(0);
        humanPlayer.setHand(deck.drawCards(CARDS_PER_PLAYER));
        humanPlayer.setWonCards(new ArrayList<>());
        humanPlayer.setActive(true);
        humanPlayer.setScore(0);

        Player aiPlayer = new Player();
        aiPlayer.setId("ai-" + UUID.randomUUID().toString());
        aiPlayer.setUsername("AI Player");
        aiPlayer.setHand(deck.drawCards(CARDS_PER_PLAYER));
        aiPlayer.setWonCards(new ArrayList<>());
        aiPlayer.setActive(true);
        aiPlayer.setScore(0);
        aiPlayer.setGame(this);

        getPlayers().add(aiPlayer);

        setGameState(HUMAN_PLAYER, humanPlayer.getId());
        setGameState(AI_PLAYER, aiPlayer.getId());

        boolean humanStarts = new Random().nextBoolean();
        setCurrentPlayer(humanStarts ? humanPlayer : aiPlayer);

        setGameState(DECK, deck);
        setGameState(CURRENT_TRICK, new ArrayList<Card>());
        getGameState().remove(TRICK_STARTER);
        getGameState().remove(CAN_HIT);
        getGameState().remove(LAST_PLAYED_CARD);
        getGameState().remove(LAST_PLAYER);

        if (!humanStarts) {
            executeAIMove();
        }
    }

    public void initializeGame_Factory_2(){
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        deck.shuffle();

        for (Player player : getPlayers()) {
            List<Card> hand = deck.drawCards(CARDS_PER_PLAYER);
            player.setHand(hand);
            player.setWonCards(new ArrayList<>());
            player.setActive(true);
            player.setScore(0);
        }

        setCurrentPlayer(getPlayers().get(0));
        setGameState(DECK, deck);
        setGameState(CURRENT_TRICK, new ArrayList<Card>());
        getGameState().remove(TRICK_STARTER);
        getGameState().remove(CAN_HIT);
        getGameState().remove(LAST_PLAYED_CARD);
        getGameState().remove(LAST_PLAYER);
    }

    public void initializeGame_Factory_4(){
    }

    protected void processGameStateObjects_1() {
        if (getGameState().containsKey(CURRENT_TRICK)) {
            Object currentTrickObj = getGameState(CURRENT_TRICK);
            List<Card> cardList = ZsirGameUtils.convertToCardList(currentTrickObj);
            setGameState(CURRENT_TRICK, cardList);
        }

        if (getGameState().containsKey(LAST_PLAYED_CARD)) {
            Object cardObj = getGameState(LAST_PLAYED_CARD);
            Card card = ZsirGameUtils.convertToCard(cardObj);
            if (card != null) {
                setGameState(LAST_PLAYED_CARD, card);
            }
        }
    }

    protected void processGameStateObjects_2() {
        if (getGameState().containsKey(CURRENT_TRICK)) {
            Object currentTrickObj = getGameState(CURRENT_TRICK);
            List<Card> cardList = ZsirGameUtils.convertToCardList(currentTrickObj);
            setGameState(CURRENT_TRICK, cardList);
        }

        if (getGameState().containsKey(LAST_PLAYED_CARD)) {
            Object cardObj = getGameState(LAST_PLAYED_CARD);
            Card card = ZsirGameUtils.convertToCard(cardObj);
            if (card != null) {
                setGameState(LAST_PLAYED_CARD, card);
            }
        }
    }

    protected void processGameStateObjects_4() {
    }

    public boolean isValidMove_1(String playerId, GameAction action) {
        String aiPlayerId = (String) getGameState(AI_PLAYER);
        if (aiPlayerId != null && aiPlayerId.equals(getCurrentPlayer().getId()) && !playerId.equals(aiPlayerId)) {
            return false;
        }

        if (action == null) {
            return false;
        }

        if ("pass".equals(action.getActionType())) {
            if (!getCurrentPlayer().getId().equals(playerId)) {
                return false;
            }

            Boolean canHit = (Boolean) getGameState(CAN_HIT);
            return Boolean.TRUE.equals(canHit);
        }

        if (!"playCard".equals(action.getActionType())) {
            return false;
        }

        if (!getCurrentPlayer().getId().equals(playerId)) {
            return false;
        }

        Card card = action.getCardParameter("card");
        if (card == null) {
            return false;
        }

        Player player = getPlayerById(playerId);
        if (player == null || !ZsirGameUtils.playerHasCard(player, card)) {
            return false;
        }

        List<Card> currentTrick = fetchCurrentTrickCards();
        return currentTrick.size() < MAX_CARDS_IN_TRICK;
    }

    public boolean isValidMove_2(String playerId, GameAction action) {
        if (action == null) {
            return false;
        }

        if ("pass".equals(action.getActionType())) {
            if (!getCurrentPlayer().getId().equals(playerId)) {
                return false;
            }

            Boolean canHit = (Boolean) getGameState(CAN_HIT);
            return Boolean.TRUE.equals(canHit);
        }

        if (!"playCard".equals(action.getActionType())) {
            return false;
        }

        if (!getCurrentPlayer().getId().equals(playerId)) {
            return false;
        }

        Card card = action.getCardParameter("card");
        if (card == null) {
            return false;
        }

        Player player = getPlayerById(playerId);
        if (player == null || !ZsirGameUtils.playerHasCard(player, card)) {
            return false;
        }

        List<Card> currentTrick = fetchCurrentTrickCards();
        return currentTrick.size() < MAX_CARDS_IN_TRICK;
    }

    public boolean isValidMove_4(String playerId, GameAction action) {
        return false;
    }

    public void executeMove_1(String playerId, GameAction action) {
        String aiPlayerId = (String) getGameState(AI_PLAYER);
        if (aiPlayerId != null && aiPlayerId.equals(getCurrentPlayer().getId())) {
            executeAIMove();
            return;
        }

        if ("pass".equals(action.getActionType())) {
            this.handlePass(playerId);

            if (getCurrentPlayer().getId().equals(aiPlayerId)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                executeAIMove();
            }
            return;
        }

        Player player = getPlayerById(playerId);
        Card card = action.getCardParameter("card");

        ZsirGameUtils.removeCardFromHand(player, card);

        List<Card> currentTrick = fetchCurrentTrickCards();
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);

        setGameState(LAST_PLAYED_CARD, card);
        setGameState(LAST_PLAYER, playerId);

        if (currentTrick.size() == 1) {
            setGameState(TRICK_STARTER, playerId);
        }

        String trickStarter = (String) getGameState(TRICK_STARTER);
        boolean isStarterTurn = playerId.equals(trickStarter);

        boolean playerHit = isPlayerHit(currentTrick, card);
        setGameState(CAN_HIT, false);

        boolean trickComplete = false;

        if (currentTrick.size() >= 2) {
            if (!isStarterTurn && !playerHit) {
                trickComplete = true;
            } else if (!isStarterTurn) {
                boolean firstPlayerCanHit = ZsirGameUtils.checkIfPlayerCanHit(getPlayerById(trickStarter), card);
                setGameState(CAN_HIT, firstPlayerCanHit);

                if (!firstPlayerCanHit) {
                    trickComplete = true;
                }
            }
        }

        if (currentTrick.size() == MAX_CARDS_IN_TRICK) {
            trickComplete = true;
        }

        if (trickComplete) {
            Player winner = determineTrickWinner(currentTrick, trickStarter);
            winner.getWonCards().addAll(currentTrick);
            addScore(winner, currentTrick);

            setGameState(CURRENT_TRICK, new ArrayList<Card>());
            getGameState().remove(TRICK_STARTER);

            setGameState(CAN_HIT, false);

            checkAndDrawCards();
            setCurrentPlayer(winner);
            serializeGameState();
        } else {
            Player nextPlayer = ZsirGameUtils.getOtherPlayer(getPlayers(), player);
            setCurrentPlayer(nextPlayer);
        }
        serializeGameState();

        if (getCurrentPlayer().getId().equals(aiPlayerId)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            executeAIMove();
        }
    }

    public void executeMove_2(String playerId, GameAction action) {
        if ("pass".equals(action.getActionType())) {
            this.handlePass(playerId);
            return;
        }

        Player player = getPlayerById(playerId);
        Card card = action.getCardParameter("card");

        ZsirGameUtils.removeCardFromHand(player, card);

        List<Card> currentTrick = fetchCurrentTrickCards();
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);

        setGameState(LAST_PLAYED_CARD, card);
        setGameState(LAST_PLAYER, playerId);

        if (currentTrick.size() == 1) {
            setGameState(TRICK_STARTER, playerId);
        }

        String trickStarter = (String) getGameState(TRICK_STARTER);
        boolean isStarterTurn = playerId.equals(trickStarter);

        boolean playerHit = isPlayerHit(currentTrick, card);
        setGameState(CAN_HIT, false);

        boolean trickComplete = false;

        if (currentTrick.size() >= 2) {
            if (!isStarterTurn && !playerHit) {
                trickComplete = true;
            } else if (!isStarterTurn) {
                boolean firstPlayerCanHit = ZsirGameUtils.checkIfPlayerCanHit(getPlayerById(trickStarter), card);
                setGameState(CAN_HIT, firstPlayerCanHit);

                if (!firstPlayerCanHit) {
                    trickComplete = true;
                }
            }
        }

        if (currentTrick.size() == MAX_CARDS_IN_TRICK) {
            trickComplete = true;
        }

        if (trickComplete) {
            Player winner = determineTrickWinner(currentTrick, trickStarter);
            winner.getWonCards().addAll(currentTrick);
            addScore(winner, currentTrick);


            setGameState(CURRENT_TRICK, new ArrayList<Card>());
            getGameState().remove(TRICK_STARTER);

            setGameState(CAN_HIT, false);

            checkAndDrawCards();
            setCurrentPlayer(winner);
            serializeGameState();
        } else {
            Player nextPlayer = ZsirGameUtils.getOtherPlayer(getPlayers(), player);
            setCurrentPlayer(nextPlayer);
        }
        serializeGameState();
    }

    public void executeMove_4(String playerId, GameAction action) {
    }

    private static boolean isPlayerHit(List<Card> currentTrick, Card card) {
        boolean playerHit = false;

        if (currentTrick.size() > 1) {
            Card leadCard = currentTrick.get(0);
            playerHit = card.getRank() == Rank.SEVEN || card.getRank() == leadCard.getRank();
        }
        return playerHit;
    }

    public List<Card> fetchCurrentTrickCards() {
        Object currentTrickObj = getGameState(CURRENT_TRICK);
        return ZsirGameUtils.convertToCardList(currentTrickObj);
    }

    public Player determineTrickWinner(List<Card> currentTrick, String trickStarter) {
        if (currentTrick.isEmpty()) {
            return getPlayers().get(0);
        }

        Card leadCard = currentTrick.get(0);
        int lastMatchingIndex = -1;

        for (int i = 0; i < currentTrick.size(); i++) {
            Card card = currentTrick.get(i);
            if (card.getRank() == Rank.SEVEN || card.getRank() == leadCard.getRank()) {
                lastMatchingIndex = i;
            }
        }

        if (lastMatchingIndex == -1) {
            return getPlayerById(trickStarter);
        }

        Player winner;
        if (lastMatchingIndex % 2 == 0) {
            winner = getPlayerById(trickStarter);
        } else {
            winner = ZsirGameUtils.getOtherPlayer(getPlayers(), getPlayerById(trickStarter));
        }

        return winner;
    }

    public void checkAndDrawCards() {
        Deck deck = (Deck) getGameState(DECK);

        if (deck == null || deck.isEmpty()) {
            return;
        }

        for (Player player : getPlayers()) {
            int cardsNeeded = CARDS_PER_PLAYER - player.getHand().size();

            if (cardsNeeded > 0) {
                List<Card> newCards = deck.drawCards(cardsNeeded);
                if (!newCards.isEmpty()) {
                    player.getHand().addAll(newCards);
                    setGameState(DECK, deck);
                }
            }

            if (deck.isEmpty()) {
                break;
            }
        }
    }

    @Override
    public boolean isGameOver() {
        Deck deck = (Deck) getGameState(DECK);
        if (deck == null) {
            return false;
        }

        if (!deck.isEmpty()) {
            return false;
        }

        for (Player player : getPlayers()) {
            if (!player.getHand().isEmpty()) {
                return false;
            }
        }

        List<Card> currentTrick = fetchCurrentTrickCards();
        return currentTrick.isEmpty();
    }

    @Override
    public Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();

        for (Player player : getPlayers()) {
            int score = 0;
            for (Card card : player.getWonCards()) {
                if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                    score += card.getValue();
                }
            }
            player.setScore(score);
            scores.put(player.getId(), score);
        }

        return scores;
    }

    @Override
    public int getMinPlayers() {
        switch (factoryId){
            case 1:
                return 1;
            case 2:
                return 2;
            case 4:
                return 4;
            default:
                defaultSwithThrow();
        }
        return 0;
    }

    @Override
    public int getMaxPlayers() {
        switch (factoryId){
            case 1:
                return 1;
            case 2:
                return 2;
            case 4:
                return 4;
            default:
                defaultSwithThrow();
        }
        return 0;
    }

    public Player getPlayerById(String playerId) {
        for (Player player : getPlayers()) {
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }

    public void addScore(Player winner, List<Card> currentTrick) {
        for (Card card : currentTrick) {
            winner.setScore(winner.getScore() + card.getValue());
        }
    }

    private void defaultSwithThrow(){
        logger.error("Invalid factoryId: {}", factoryId);
        throw new IllegalArgumentException("Invalid factoryId: " + factoryId);
    }

    public void handlePass(String playerId) {
        List<Card> currentTrick = fetchCurrentTrickCards();
        String trickStarter = (String) getGameState(TRICK_STARTER);

        if (currentTrick.isEmpty()) {
            return;
        }

        setGameState(CAN_HIT, false);

        Player winner = determineTrickWinner(currentTrick, trickStarter);
        winner.getWonCards().addAll(currentTrick);
        addScore(winner, currentTrick);

        setGameState(CURRENT_TRICK, new ArrayList<Card>());
        getGameState().remove(TRICK_STARTER);

        checkAndDrawCards();
        setCurrentPlayer(winner);
    }

    public void recordAbandonedUser(String userId) {
        List<String> abandonedUsers = new ArrayList<>();
        if (hasGameState(ABANDONED_USERS)) {
            Object obj = getGameState(ABANDONED_USERS);
            if (obj instanceof List) {
                abandonedUsers = (List<String>) obj;
            }
        }

        if (!abandonedUsers.contains(userId)) {
            abandonedUsers.add(userId);
            setGameState(ABANDONED_USERS, abandonedUsers);
        }

        serializeGameState();
    }

    public List<String> fetchAbandonedUsers() {
        if (hasGameState(ABANDONED_USERS)) {
            Object obj = getGameState(ABANDONED_USERS);
            if (obj instanceof List) {
                return (List<String>) obj;
            }
        }
        return new ArrayList<>();
    }

    private void executeAIMove() {
        String aiPlayerId = (String) getGameState(AI_PLAYER);
        String humanPlayerId = (String) getGameState(HUMAN_PLAYER);

        Player aiPlayer = getPlayerById(aiPlayerId);
        Player humanPlayer = getPlayerById(humanPlayerId);

        if (aiPlayer == null || humanPlayer == null) {
            logger.error("AI or human player not found");
            return;
        }

        ZsirGameAI ai = new ZsirGameAI(this, aiPlayer, humanPlayer);
        GameAction aiAction = ai.decideMove();

        if (aiAction == null) {
            logger.error("AI failed to decide on a move");
            return;
        }

        if ("pass".equals(aiAction.getActionType())) {
            this.handlePass(aiPlayerId);
            return;
        }

        Card card = aiAction.getCardParameter("card");

        if (card == null) {
            logger.error("AI action has no card parameter");
            return;
        }

        ZsirGameUtils.removeCardFromHand(aiPlayer, card);

        List<Card> currentTrick = fetchCurrentTrickCards();
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);

        setGameState(LAST_PLAYED_CARD, card);
        setGameState(LAST_PLAYER, aiPlayerId);

        if (currentTrick.size() == 1) {
            setGameState(TRICK_STARTER, aiPlayerId);
        }

        String trickStarter = (String) getGameState(TRICK_STARTER);
        boolean isStarterTurn = aiPlayerId.equals(trickStarter);

        boolean playerHit = isPlayerHit(currentTrick, card);
        setGameState(CAN_HIT, false);

        boolean trickComplete = false;

        if (currentTrick.size() >= 2) {
            if (!isStarterTurn && !playerHit) {
                trickComplete = true;
            } else if (!isStarterTurn) {
                boolean humanCanHit = ZsirGameUtils.checkIfPlayerCanHit(humanPlayer, card);
                setGameState(CAN_HIT, humanCanHit);

                if (!humanCanHit) {
                    trickComplete = true;
                }
            }
        }

        if (currentTrick.size() == MAX_CARDS_IN_TRICK) {
            trickComplete = true;
        }

        if (trickComplete) {
            Player winner = determineTrickWinner(currentTrick, trickStarter);
            winner.getWonCards().addAll(currentTrick);
            addScore(winner, currentTrick);

            setGameState(CURRENT_TRICK, new ArrayList<Card>());
            getGameState().remove(TRICK_STARTER);

            setGameState(CAN_HIT, false);

            checkAndDrawCards();
            setCurrentPlayer(winner);

            if (winner.getId().equals(aiPlayerId) && getCurrentPlayer().getId().equals(aiPlayerId)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                executeAIMove();
            }
        } else {
            setCurrentPlayer(humanPlayer);
        }

        serializeGameState();
    }
}
