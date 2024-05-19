package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     * Mapping the tokens for each player.
     */
    protected final ArrayList<LinkedList<Integer>> PleyersTokens;

    protected BlockingQueue<Integer> waitingPlayers;

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.PleyersTokens = new ArrayList<>(env.config.players);
        for (int i = 0; i < env.config.players; i++) {
            PleyersTokens.add(new LinkedList<>());
        }
        waitingPlayers = new LinkedBlockingQueue<>(env.config.players);
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        cardToSlot[card] = slot;
        slotToCard[slot] = card;

        // TODO implement
        env.ui.placeCard(card, slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        // TODO implement
        
        cardToSlot[slotToCard[slot]] = null;
        slotToCard[slot] = null;
      
        for(LinkedList<Integer> player: PleyersTokens){
            if(player.contains(slot)){
                int index = player.indexOf(slot);
                if(index >= 0 && player.size()==1){
                    player.remove(0);
                }
                else{
                    player.remove(index);
                }
            }
        }
        env.ui.removeTokens(slot);
        env.ui.removeCard(slot);
    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        // TODO implement
        if(slotToCard[slot] != null){
            PleyersTokens.get(player).add(slot);
            env.ui.placeToken(player, slot);
            if (PleyersTokens.get(player).size() == 3) {
                this.notifyAll();   
                // to wake up the dealer           
            }
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement
        if (PleyersTokens.get(player).contains(slot)) {
            int index = PleyersTokens.get(player).indexOf(slot);
            PleyersTokens.get(player).remove(index);
            env.ui.removeToken(player,slot);
            return true;
        }
        return false;
    }

    public int[] getCardssWithTokens(int id)  {
        int[] arr = new int[PleyersTokens.get(id).size()];
        for(int i=0; i<PleyersTokens.get(id).size(); i++){
            arr[i] = slotToCard[PleyersTokens.get(id).get(i)];
        }
        return arr; 
        }


    public LinkedList<Integer> getTokens(int player) {
        return PleyersTokens.get(player);
    }
    
    public boolean hasSet(){
        List<Integer> cards = new ArrayList<>();
        for(int i=0; i < slotToCard.length; i++){
            if(slotToCard[i]!=null){
                cards.add(slotToCard[i]);
            }
        }
        return(env.util.findSets(cards,1).size()>0);
    }
}
