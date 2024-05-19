package bguspl.set.ex;

import bguspl.set.Env;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    private long lastUpdateTime = System.currentTimeMillis();

    private Thread[] playersThreads;

    private final int SECOND = 1000;

    private final int MILISECOND = 1;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        playersThreads = new Thread[players.length];
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        createAndRunPlayersThreads();
        while (!shouldFinish()) {
            placeCardsOnTable();// עשינו
            timerLoop();// בתהליכים
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        announceWinners();
        stopAllRunningPlayers();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        this.terminate = true;
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement
        synchronized (table) {
            if (table.waitingPlayers.size() > 0) {
                try {
                    Player player = players[table.waitingPlayers.take()];
                    int[] cards = player.getTokens();
                    boolean isSet = env.util.testSet(cards);
                    if (!isSet) {
                        player.setPenaltyOrPoint(-1);
                    } else {
                        player.setPenaltyOrPoint(1);

                        for (Player p : players) {
                            if (p != player) {
                                p.removeTokens(cards);
                                if(table.waitingPlayers.contains(p.id) && p.numOfTokens() < env.config.featureSize) {
                                    
                                    table.waitingPlayers.remove(p.id);
                                    synchronized (p) {
                                        p.notifyAll();
                                    }
                                }
                                p.clearActions();
                            }    
                        }
                        for (int i = 0; i < cards.length; i++) {
                            if (table.cardToSlot[cards[i]] < env.config.tableSize) {
                                table.removeCard(table.cardToSlot[cards[i]]);
                            }
                        }
                    }
                    synchronized (player) {
                        player.notifyAll();
                    }
                }
                catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement
        synchronized (table) {
            if (table.countCards() < env.config.tableSize) {
                for (int i = 0; i < env.config.tableSize; i++) {
                    if (table.slotToCard[i] == null) {
                        if (deck.size() > 0) {
                            Random rand = new Random();
                            int randomIndex = rand.nextInt(deck.size());
                            int card = deck.remove(randomIndex);
                            table.placeCard(card, i);
                        }
                    }
                }
                if (!deck.isEmpty()) {
                    updateTimerDisplay(true);
                }
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        if (reshuffleTime - System.currentTimeMillis() <= env.config.turnTimeoutWarningMillis) {
            try {
                synchronized (table) {
                    table.wait(MILISECOND);
                }
            } catch (InterruptedException e) {
            }
        } else {
            try {
                synchronized (table) {
                    table.wait(SECOND);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement
        if (reset) {
            reshuffleTime = env.config.turnTimeoutMillis; // Reset reshuffleTime to 1 minute
            env.ui.setCountdown(reshuffleTime, false);
            lastUpdateTime = System.currentTimeMillis();
            reshuffleTime = reshuffleTime + lastUpdateTime;

        } else if (reshuffleTime - System.currentTimeMillis() <= env.config.turnTimeoutWarningMillis) {
            lastUpdateTime = System.currentTimeMillis();
            env.ui.setCountdown(Math.abs(reshuffleTime - lastUpdateTime), true);
        } else if (System.currentTimeMillis() - lastUpdateTime >= SECOND) {

            env.ui.setCountdown(reshuffleTime - SECOND - lastUpdateTime, false);
            lastUpdateTime = lastUpdateTime + SECOND;
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        for (int i = 0; i < env.config.tableSize; i++) {
            if (table.slotToCard[i] != null) {
                deck.add(table.slotToCard[i]);
                table.removeCard(i);
            }
        }
        if (!deck.isEmpty()) {
            Collections.shuffle(deck);
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        Player winner = null;
        for (Player player : players) {
            if (winner == null || player.score() > winner.score()) {
                winner = player;
            }
        }

        if (players[0].score() == players[1].score()) {
            env.ui.setScore(players[0].id, players[0].score());
            env.ui.setScore(players[1].id, players[1].score());
            env.ui.announceWinner(new int[] { players[0].id, players[1].id });
        } else {
            env.ui.setScore(winner.id, winner.score());
            env.ui.announceWinner(new int[] { winner.id });
        }
    }

    private void createAndRunPlayersThreads() {
        for (int i = 0; i < playersThreads.length; i++) {
            playersThreads[i] = new Thread(this.players[i], "Player number " + i);
            synchronized (players[i]) {

                playersThreads[i].start();
                try {
                    players[i].wait();
                } catch (InterruptedException ignored) {
                }

            }
        }
    }

    private void stopAllRunningPlayers() {
        for (int i = playersThreads.length-1; i >=0 ; i--) {
            synchronized (players[i]) {
                players[i].terminate();            
                }
        }
    }
}
