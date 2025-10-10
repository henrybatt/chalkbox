package chalkbox.api.common.java;

import chalkbox.stages.Visibility;

public record JUnitIndividualResult(int passes, int fails, int total, String output, String outputFormat, String name, int weight, double classWeight, Visibility visibility) { }
