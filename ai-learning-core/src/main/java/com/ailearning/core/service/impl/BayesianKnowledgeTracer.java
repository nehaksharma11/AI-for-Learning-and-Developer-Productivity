package com.ailearning.core.service.impl;

import com.ailearning.core.model.LearningProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements Bayesian Knowledge Tracing for skill modeling and learning progress tracking.
 * Uses probabilistic models to estimate knowledge state and predict learning outcomes.
 */
public class BayesianKnowledgeTracer {

    private static final Logger logger = LoggerFactory.getLogger(BayesianKnowledgeTracer.class);

    // BKT model parameters
    private static final double DEFAULT_PRIOR_KNOWLEDGE = 0.1; // P(L0) - initial knowledge probability
    private static final double DEFAULT_LEARNING_RATE = 0.3;   // P(T) - probability of learning from instruction
    private static final double DEFAULT_GUESS_RATE = 0.2;      // P(G) - probability of guessing correctly
    private static final double DEFAULT_SLIP_RATE = 0.1;       // P(S) - probability of making a mistake when knowing

    // Knowledge state tracking per skill domain
    private final Map<String, KnowledgeState> knowledgeStates = new HashMap<>();

    /**
     * Updates knowledge state based on learning progress evidence.
     */
    public void updateKnowledge(LearningProgress progress) {
        logger.info("Updating knowledge state for skill: {}", progress.getSkillDomain());

        String skillDomain = progress.getSkillDomain();
        KnowledgeState currentState = knowledgeStates.getOrDefault(skillDomain, 
                new KnowledgeState(DEFAULT_PRIOR_KNOWLEDGE, DEFAULT_LEARNING_RATE, 
                                 DEFAULT_GUESS_RATE, DEFAULT_SLIP_RATE));

        // Update based on performance evidence
        boolean correctResponse = progress.getCompletionPercentage() > 0.7; // Consider >70% as correct
        KnowledgeState updatedState = updateBayesianModel(currentState, correctResponse);

        knowledgeStates.put(skillDomain, updatedState);

        logger.debug("Updated knowledge probability for {}: {:.3f}", 
                    skillDomain, updatedState.knowledgeProbability);
    }

    /**
     * Gets current knowledge probability for a skill domain.
     */
    public double getKnowledgeProbability(String skillDomain) {
        KnowledgeState state = knowledgeStates.get(skillDomain);
        return state != null ? state.knowledgeProbability : DEFAULT_PRIOR_KNOWLEDGE;
    }

    /**
     * Predicts the probability of success on the next learning activity.
     */
    public double predictSuccessProbability(String skillDomain) {
        KnowledgeState state = knowledgeStates.getOrDefault(skillDomain, 
                new KnowledgeState(DEFAULT_PRIOR_KNOWLEDGE, DEFAULT_LEARNING_RATE, 
                                 DEFAULT_GUESS_RATE, DEFAULT_SLIP_RATE));

        // P(correct) = P(L) * (1 - P(S)) + (1 - P(L)) * P(G)
        return state.knowledgeProbability * (1 - state.slipRate) + 
               (1 - state.knowledgeProbability) * state.guessRate;
    }

    /**
     * Estimates the number of learning opportunities needed to reach mastery.
     */
    public int estimateLearningOpportunities(String skillDomain, double masteryThreshold) {
        double currentKnowledge = getKnowledgeProbability(skillDomain);
        
        if (currentKnowledge >= masteryThreshold) {
            return 0;
        }

        KnowledgeState state = knowledgeStates.getOrDefault(skillDomain, 
                new KnowledgeState(DEFAULT_PRIOR_KNOWLEDGE, DEFAULT_LEARNING_RATE, 
                                 DEFAULT_GUESS_RATE, DEFAULT_SLIP_RATE));

        // Estimate opportunities using learning rate
        double knowledgeGap = masteryThreshold - currentKnowledge;
        return (int) Math.ceil(knowledgeGap / state.learningRate) + 1;
    }

    /**
     * Updates the Bayesian model based on observed performance.
     */
    private KnowledgeState updateBayesianModel(KnowledgeState currentState, boolean correctResponse) {
        double priorKnowledge = currentState.knowledgeProbability;
        double learningRate = currentState.learningRate;
        double guessRate = currentState.guessRate;
        double slipRate = currentState.slipRate;

        // Calculate likelihood of the observed response
        double likelihoodCorrect, likelihoodIncorrect;
        
        if (correctResponse) {
            // P(correct | learned) = 1 - slip, P(correct | not learned) = guess
            likelihoodCorrect = priorKnowledge * (1 - slipRate) + (1 - priorKnowledge) * guessRate;
            
            // Update using Bayes' theorem
            double posteriorKnowledge = (priorKnowledge * (1 - slipRate)) / likelihoodCorrect;
            
            // Account for learning opportunity
            posteriorKnowledge = posteriorKnowledge + (1 - posteriorKnowledge) * learningRate;
            
            return new KnowledgeState(posteriorKnowledge, learningRate, guessRate, slipRate);
        } else {
            // P(incorrect | learned) = slip, P(incorrect | not learned) = 1 - guess
            likelihoodIncorrect = priorKnowledge * slipRate + (1 - priorKnowledge) * (1 - guessRate);
            
            // Update using Bayes' theorem
            double posteriorKnowledge = (priorKnowledge * slipRate) / likelihoodIncorrect;
            
            // Account for learning opportunity (even from mistakes)
            posteriorKnowledge = posteriorKnowledge + (1 - posteriorKnowledge) * (learningRate * 0.5);
            
            return new KnowledgeState(posteriorKnowledge, learningRate, guessRate, slipRate);
        }
    }

    /**
     * Adapts model parameters based on individual learner characteristics.
     */
    public void adaptModelParameters(String skillDomain, double observedLearningRate, 
                                   double observedGuessRate, double observedSlipRate) {
        KnowledgeState currentState = knowledgeStates.getOrDefault(skillDomain, 
                new KnowledgeState(DEFAULT_PRIOR_KNOWLEDGE, DEFAULT_LEARNING_RATE, 
                                 DEFAULT_GUESS_RATE, DEFAULT_SLIP_RATE));

        // Blend observed parameters with current parameters
        double adaptedLearningRate = (currentState.learningRate + observedLearningRate) / 2;
        double adaptedGuessRate = (currentState.guessRate + observedGuessRate) / 2;
        double adaptedSlipRate = (currentState.slipRate + observedSlipRate) / 2;

        KnowledgeState adaptedState = new KnowledgeState(currentState.knowledgeProbability,
                adaptedLearningRate, adaptedGuessRate, adaptedSlipRate);

        knowledgeStates.put(skillDomain, adaptedState);

        logger.info("Adapted BKT parameters for {}: L={:.3f}, G={:.3f}, S={:.3f}", 
                   skillDomain, adaptedLearningRate, adaptedGuessRate, adaptedSlipRate);
    }

    /**
     * Resets knowledge state for a skill domain.
     */
    public void resetKnowledgeState(String skillDomain) {
        knowledgeStates.remove(skillDomain);
        logger.info("Reset knowledge state for skill domain: {}", skillDomain);
    }

    /**
     * Gets all tracked skill domains and their knowledge probabilities.
     */
    public Map<String, Double> getAllKnowledgeProbabilities() {
        Map<String, Double> probabilities = new HashMap<>();
        knowledgeStates.forEach((skill, state) -> 
                probabilities.put(skill, state.knowledgeProbability));
        return probabilities;
    }

    /**
     * Internal class to represent knowledge state with BKT parameters.
     */
    private static class KnowledgeState {
        final double knowledgeProbability; // P(L) - current knowledge probability
        final double learningRate;         // P(T) - probability of learning
        final double guessRate;            // P(G) - probability of guessing correctly
        final double slipRate;             // P(S) - probability of making mistakes

        KnowledgeState(double knowledgeProbability, double learningRate, 
                      double guessRate, double slipRate) {
            this.knowledgeProbability = Math.max(0.0, Math.min(1.0, knowledgeProbability));
            this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
            this.guessRate = Math.max(0.0, Math.min(1.0, guessRate));
            this.slipRate = Math.max(0.0, Math.min(1.0, slipRate));
        }
    }
}