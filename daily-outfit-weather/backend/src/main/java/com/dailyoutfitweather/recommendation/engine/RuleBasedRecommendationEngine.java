package com.dailyoutfitweather.recommendation.engine;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.dto.RecommendationResult;

@Component
public class RuleBasedRecommendationEngine {

	private final WeatherConditionAnalyzer weatherConditionAnalyzer;
	private final RecommendationRuleEngine recommendationRuleEngine;
	private final OutfitSelector outfitSelector;
	private final ItemSelector itemSelector;
	private final RecommendationMessageGenerator messageGenerator;

	public RuleBasedRecommendationEngine(
		WeatherConditionAnalyzer weatherConditionAnalyzer,
		RecommendationRuleEngine recommendationRuleEngine,
		OutfitSelector outfitSelector,
		ItemSelector itemSelector,
		RecommendationMessageGenerator messageGenerator
	) {
		this.weatherConditionAnalyzer = weatherConditionAnalyzer;
		this.recommendationRuleEngine = recommendationRuleEngine;
		this.outfitSelector = outfitSelector;
		this.itemSelector = itemSelector;
		this.messageGenerator = messageGenerator;
	}

	public RecommendationResult recommend(RecommendationInput input) {
		AnalyzedWeatherCondition condition = weatherConditionAnalyzer.analyze(input);
		int recommendationTemperature = recommendationRuleEngine.calculateRecommendationTemperature(input, condition);
		OutfitRecommendation outfit = outfitSelector.select(recommendationTemperature);
		List<String> items = itemSelector.select(condition);
		return new RecommendationResult(
			recommendationTemperature,
			outfit.topRecommendation(),
			outfit.outerRecommendation(),
			String.join(", ", items),
			items,
			messageGenerator.generate(input.messageTone(), outfit, items, condition),
			messageGenerator.reason(condition, recommendationTemperature),
			outfit.characterImageType()
		);
	}
}
