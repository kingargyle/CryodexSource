package cryodex.modules.starwarslcg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SWLCGEliminationGeneration {

    public static List<SWLCGMatch> setupInitialBracket(SWLCGTournament tournament, int cutSize) {

        List<SWLCGMatch> matches = new ArrayList<>();
        List<SWLCGMatch> matchesCorrected = new ArrayList<SWLCGMatch>();

        List<SWLCGPlayer> tempList = new ArrayList<>();
        tempList.addAll(tournament.getSWLCGPlayers());
        Collections.sort(tempList, new SWLCGComparator(tournament, SWLCGComparator.rankingCompare));
        tempList = tempList.subList(0, cutSize);

        while (tempList.isEmpty() == false) {
            SWLCGPlayer player1 = tempList.get(0);
            SWLCGPlayer player2 = tempList.get(tempList.size() - 1);
            tempList.remove(player1);
            if (player1 == player2) {
                player2 = null;
            } else {
                tempList.remove(player2);
            }

            SWLCGMatch match = new SWLCGMatch(player1, player2);
            matches.add(match);
        }

        SWLCGMatch m = null;

        switch (matches.size()) {
        case 4:
            m = matches.get(0);
            m.setMatchLabel("w1");
            matchesCorrected.add(m);
            m = matches.get(3);
            m.setMatchLabel("w2");
            matchesCorrected.add(m);
            m = matches.get(2);
            m.setMatchLabel("w3");
            matchesCorrected.add(m);
            m = matches.get(1);
            m.setMatchLabel("w4");
            matchesCorrected.add(m);
            break;
        case 8:
            m = matches.get(0);
            m.setMatchLabel("w1");
            matchesCorrected.add(m);
            m = matches.get(7);
            m.setMatchLabel("w2");
            matchesCorrected.add(m);
            m = matches.get(4);
            m.setMatchLabel("w3");
            matchesCorrected.add(m);
            m = matches.get(3);
            m.setMatchLabel("w4");
            matchesCorrected.add(m);
            m = matches.get(2);
            m.setMatchLabel("w5");
            matchesCorrected.add(m);
            m = matches.get(5);
            m.setMatchLabel("w6");
            matchesCorrected.add(m);
            m = matches.get(6);
            m.setMatchLabel("w7");
            matchesCorrected.add(m);
            m = matches.get(1);
            m.setMatchLabel("w8");
            matchesCorrected.add(m);
            break;
        case 2:
            m = matches.get(0);
            m.setMatchLabel("w1");
            matchesCorrected.add(m);
            m = matches.get(1);
            m.setMatchLabel("w2");
            matchesCorrected.add(m);
        default:
            matchesCorrected = matches;
        }

        return matchesCorrected;
    }

    public static List<SWLCGMatch> generateNextEliminationRound(SWLCGTournament tournament) {

        List<SWLCGMatch> previousMatches = tournament.getLatestRound().getMatches();
        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();

        String match1Label = previousMatches.get(0).getMatchLabel();

        if ("w1".equals(match1Label)) {
            if (previousMatches.size() == 2) {
                matches = top4round2(previousMatches);
            } else if (previousMatches.size() == 4) {
                matches = top8round2(previousMatches);
            } else if (previousMatches.size() == 8) {
                matches = top16round2(previousMatches);
            }
        } else if ("w3".equals(match1Label)) {
            matches = top4round3(previousMatches);
        } else if ("w4".equals(match1Label)) {
            matches = top4round4(previousMatches);
        } else if ("w5".equals(match1Label)) {
            matches = top8round3(previousMatches);
        } else if ("w7".equals(match1Label)) {
            matches = top8round4(previousMatches);
        } else if ("w8".equals(match1Label)) {
            if (previousMatches.size() == 3) {
                matches = top8round5(previousMatches);
            } else if (previousMatches.size() == 2) {
                matches = top8round6(previousMatches);
            }
        } else if ("w9".equals(match1Label)) {
            matches = top16round3(previousMatches);
        } else if ("w13".equals(match1Label)) {
            matches = top16round4(previousMatches);
        } else if ("w15".equals(match1Label)) {
            matches = top16round5(previousMatches);
        } else if ("w16".equals(match1Label)) {
            if (previousMatches.size() == 3) {
                matches = top16round6(previousMatches);
            } else if (previousMatches.size() == 2) {
                matches = top16round7(previousMatches);
            }
        }

        return matches;
    }

    public static SWLCGMatch getMatch(String matchLabel, SWLCGPlayer player1, SWLCGPlayer player2) {
        return null;
    }

    public static SWLCGMatch getMatch(String matchLabel, SWLCGMatch match1, boolean getWinner1, SWLCGMatch match2, boolean getWinner2) {

        SWLCGMatch m = new SWLCGMatch();
        m.setMatchLabel(matchLabel);

        if (getWinner1) {
            m.setPlayer1(match1.getGame1Winner());
        } else {
            m.setPlayer1(match1.getGame1Loser());
        }

        if (match2 == null) {
            m.setBye(true);
        } else {
            if (getWinner2) {
                m.setPlayer2(match2.getGame1Winner());
            } else {
                m.setPlayer2(match2.getGame1Loser());
            }
        }

        return m;
    }

    public static List<SWLCGMatch> top4round2(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w3 = getMatch("w3", previousMatches.get(0), true, previousMatches.get(1), true);
        SWLCGMatch l1 = getMatch("l1", previousMatches.get(0), false, previousMatches.get(1), false);

        matches.add(w3);
        matches.add(l1);

        return matches;
    }

    public static List<SWLCGMatch> top4round3(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w4 = getMatch("w4", previousMatches.get(0), true, null, true);
        SWLCGMatch l2 = getMatch("l2", previousMatches.get(0), false, previousMatches.get(1), true);

        matches.add(w4);
        matches.add(l2);

        return matches;
    }

    public static List<SWLCGMatch> top4round4(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch f = getMatch("f", previousMatches.get(0), true, previousMatches.get(1), true);

        matches.add(f);

        return matches;
    }

    public static List<SWLCGMatch> top8round2(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w5 = getMatch("w5", previousMatches.get(0), true, previousMatches.get(1), true);
        SWLCGMatch l1 = getMatch("l1", previousMatches.get(0), false, previousMatches.get(1), false);

        SWLCGMatch w6 = getMatch("w6", previousMatches.get(2), true, previousMatches.get(3), true);
        SWLCGMatch l2 = getMatch("l2", previousMatches.get(2), false, previousMatches.get(3), false);

        matches.add(w5);
        matches.add(w6);
        matches.add(l1);
        matches.add(l2);

        return matches;
    }

    public static List<SWLCGMatch> top8round3(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w7 = getMatch("w7", previousMatches.get(0), true, previousMatches.get(1), true);
        SWLCGMatch l3 = getMatch("l3", previousMatches.get(1), false, previousMatches.get(2), true);
        SWLCGMatch l4 = getMatch("l4", previousMatches.get(0), false, previousMatches.get(3), true);

        matches.add(w7);
        matches.add(l3);
        matches.add(l4);

        return matches;
    }

    public static List<SWLCGMatch> top8round4(List<SWLCGMatch> previousMatches) {


        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w8 = getMatch("w8", previousMatches.get(0), true, null, true);
        SWLCGMatch l5 = getMatch("l5", previousMatches.get(0), false, null, true);
        SWLCGMatch l6 = getMatch("l6", previousMatches.get(1), false, previousMatches.get(2), true);

        matches.add(w8);
        matches.add(l5);
        matches.add(l6);

        return matches;
    }

    public static List<SWLCGMatch> top8round5(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w8 = getMatch("w8", previousMatches.get(0), true, null, true);
        SWLCGMatch l7 = getMatch("l7", previousMatches.get(1), true, previousMatches.get(2), true);

        matches.add(w8);
        matches.add(l7);

        return matches;
    }

    public static List<SWLCGMatch> top8round6(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch f = getMatch("f", previousMatches.get(0), true, previousMatches.get(1), true);

        matches.add(f);

        return matches;
    }

    public static List<SWLCGMatch> top16round2(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w9 = getMatch("w9", previousMatches.get(0), true, previousMatches.get(1), true);
        SWLCGMatch w10 = getMatch("w10", previousMatches.get(2), true, previousMatches.get(3), true);
        SWLCGMatch w11 = getMatch("w11", previousMatches.get(4), true, previousMatches.get(5), true);
        SWLCGMatch w12 = getMatch("w12", previousMatches.get(6), true, previousMatches.get(7), true);

        SWLCGMatch l1 = getMatch("l1", previousMatches.get(0), false, previousMatches.get(1), false);
        SWLCGMatch l2 = getMatch("l2", previousMatches.get(4), false, previousMatches.get(5), false);
        SWLCGMatch l3 = getMatch("l3", previousMatches.get(2), false, previousMatches.get(3), false);
        SWLCGMatch l4 = getMatch("l4", previousMatches.get(6), false, previousMatches.get(7), false);
        
        matches.add(w9);
        matches.add(w10);
        matches.add(w11);
        matches.add(w12);
        matches.add(l1);
        matches.add(l2);
        matches.add(l3);
        matches.add(l4);

        return matches;
    }

    public static List<SWLCGMatch> top16round3(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w13 = getMatch("w13", previousMatches.get(0), true, previousMatches.get(1), true);
        SWLCGMatch w14 = getMatch("w14", previousMatches.get(2), true, previousMatches.get(3), true);

        SWLCGMatch l5 = getMatch("l5", previousMatches.get(1), false, previousMatches.get(4), true);
        SWLCGMatch l6 = getMatch("l6", previousMatches.get(3), false, previousMatches.get(5), true);
        SWLCGMatch l7 = getMatch("l7", previousMatches.get(0), false, previousMatches.get(6), true);
        SWLCGMatch l8 = getMatch("l8", previousMatches.get(2), false, previousMatches.get(7), true);
        
        matches.add(w13);
        matches.add(w14);
        matches.add(l5);
        matches.add(l6);
        matches.add(l7);
        matches.add(l8);

        return matches;
    }

    public static List<SWLCGMatch> top16round4(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w15 = getMatch("w15", previousMatches.get(0), true, previousMatches.get(1), true);

        SWLCGMatch l9 = getMatch("l9", previousMatches.get(1), false, previousMatches.get(2), true);
        SWLCGMatch l10 = getMatch("l10", previousMatches.get(0), false, previousMatches.get(3), true);
        
        SWLCGMatch l11 = getMatch("l11", previousMatches.get(4), true, previousMatches.get(5), true);
        
        matches.add(w15);
        matches.add(l9);
        matches.add(l10);
        matches.add(l11);

        return matches;
    }

    public static List<SWLCGMatch> top16round5(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w16 = getMatch("w16", previousMatches.get(0), true, null, true);

        SWLCGMatch l12 = getMatch("l12", previousMatches.get(1), true, previousMatches.get(2), true);
        SWLCGMatch l13 = getMatch("l13", previousMatches.get(0), false, previousMatches.get(3), true);
        
        matches.add(w16);
        matches.add(l12);
        matches.add(l13);

        return matches;
    }

    public static List<SWLCGMatch> top16round6(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch w16 = getMatch("w16", previousMatches.get(0), true, null, true);

        SWLCGMatch l14 = getMatch("l14", previousMatches.get(1), true, previousMatches.get(2), true);
        
        matches.add(w16);
        matches.add(l14);

        return matches;
    }

    public static List<SWLCGMatch> top16round7(List<SWLCGMatch> previousMatches) {

        List<SWLCGMatch> matches = new ArrayList<SWLCGMatch>();
        SWLCGMatch f = getMatch("f", previousMatches.get(0), true, previousMatches.get(1), true);

        matches.add(f);

        return matches;
    }
}
