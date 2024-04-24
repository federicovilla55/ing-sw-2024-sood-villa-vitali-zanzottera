package it.polimi.ingsw.gc19.View.GameLocalView;

public abstract class LocalStationPlayer {
    /*private final String ownerPlayer;
    // cards in hand
    // private goal card
    private int numPoints;
    private final PlayableCard[][] cardSchema;
    private final int[][] cardOverlap;
    private int currentCount;
    private final HashMap<PlayableCard, Tuple<Integer, Integer>> cardPosition;
    private final GoalCard[] privateGoalCardsInStation;

    private final Map<Symbol, Integer> visibleSymbols;

    private final List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    private Color chosenColor;


    public LocalStationPlayer(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                              int numPoints, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        this.cardOverlap = new int[ImportantConstants.gridDimension][ImportantConstants.gridDimension];

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                this.cardSchema[i][k] = null;
                this.cardOverlap[i][k] = 0;
            }
        }

        this.cardPosition = new HashMap<>();
        this.currentCount = 0;


        this.ownerPlayer = nicknameOwner;
        this.chosenColor = chosenColor;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.placedCardSequence = placedCardSequence;
    }

    public void reconstructSchema() {
        // Reset card schema and overlap
        for (int i = 0; i < ImportantConstants.gridDimension; i++) {
            for (int k = 0; k < ImportantConstants.gridDimension; k++) {
                cardSchema[i][k] = null;
                cardOverlap[i][k] = 0;
            }
        }

        // Reconstruct card schema and overlap from placedCardSequence
        for (Tuple<PlayableCard, Tuple<Integer,Integer>> placement : placedCardSequence) {
            PlayableCard card = placement.x();
            Tuple<Integer, Integer> position = placement.y();
            int rowIndex = position.x();
            int colIndex = position.y();

            // Update card schema with the placed card
            cardSchema[rowIndex][colIndex] = card;

            // Increment the overlap count for the corresponding position
            cardOverlap[rowIndex][colIndex]++;

            // Store the position of the card
            cardPosition.put(card, new Tuple<>(rowIndex, colIndex));

            // Increment currentCount
            currentCount++;
        }
    }*/



}
