package bguspl.set.ex;

import java.util.Random;
import bguspl.set.Env;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate
     * key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    private BlockingQueue<Integer> actions;

    private int peneltyOrPoint = 0;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided
     *               manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        actions = new LinkedBlockingQueue<Integer>(env.config.featureSize);
    }

    /**
     * The main player thread of each player starts here (main loop for the player
     * thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human)
            createArtificialIntelligence();

        synchronized (this) {
            notifyAll();
        }
        while (!terminate) {

            // TODO implement main player loop
            try {
                Integer nextAction = actions.take();
                if (table.getTokens(id).contains(nextAction)) {
                    synchronized (table) {
                        table.removeToken(id, nextAction);
                    }
                } else {
                    boolean wasAdded = false;
                    synchronized (table) {
                        if (table.getTokens(id).size() < env.config.featureSize
                                && table.slotToCard[nextAction] != null) {
                            table.placeToken(id, nextAction);
                            wasAdded = true;
                        }
                    }
                    if (numOfTokens() == env.config.featureSize && wasAdded) {
                        synchronized (this) {
                            table.waitingPlayers.add(this.id);
                            try {
                                wait();
                            } catch (InterruptedException ignored) {
                            }
                            if (peneltyOrPoint == -1)
                                penalty();
                            if (peneltyOrPoint == 1)
                                point();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
        if (!human) {
            try {
                aiThread.join();
            } catch (InterruptedException ignored) {
            }
        }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of
     * this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it
     * is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator

                Random x = new Random();
                int randomSlot = x.nextInt(env.config.tableSize);
                keyPressed(randomSlot);

            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        this.terminate = true;
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement

        if (actions.size() < env.config.featureSize) {
            try {
                actions.put(slot);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement
        if (!actions.isEmpty()) {
            actions.clear();
        }
        score = score + peneltyOrPoint;
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        env.ui.setScore(id, score);
        int second = 1000;
        for (int i = 0; i < env.config.pointFreezeMillis & !terminate; i = i + second) {
            env.ui.setFreeze(id, env.config.pointFreezeMillis - i);
            try {
                playerThread.sleep(second);
            } catch (InterruptedException e) {
            }
        }
        actions.clear();
        env.ui.setFreeze(id, 0);
        peneltyOrPoint = 0;
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement

        // make the player (thread) that choose ilegal set to freeze for 5 seconds
        Long freezeTime = env.config.penaltyFreezeMillis;
        int second = 1000;
        for (int i = 0; i < freezeTime & !terminate; i = i + second) {
            env.ui.setFreeze(id, freezeTime - i);
            try {
                playerThread.sleep(second);
            } catch (InterruptedException e) {
            }
        }
        actions.clear();
        env.ui.setFreeze(id, 0);
        peneltyOrPoint = 0;
    }

    public int numOfTokens() {
        return table.getTokens(id).size();
    }

    public int[] getTokens() {
        return table.getCardssWithTokens(id);
    }

    public int score() {
        return score;
    }

    public void setPenaltyOrPoint(int i) {
        this.peneltyOrPoint = i;
    }

    public void removeTokens(int[] cards) {
        for (int i = 0; i < cards.length; i++) {
            int j = table.cardToSlot[cards[i]];
            if (table.getTokens(id).contains(j)) {
                table.removeToken(id, j);
            }
        }
    }

    public void clearActions() {
        actions.clear();
    }

}
