package io.github.uoy.eng1.c2t7.goosepatrol.world;

/** Summary of a finished run. */
public record GameResult(
    boolean won,
    float secondsUsed,
    int itemsUsed,
    int eventsTriggered

) {

}