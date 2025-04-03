package com.capstone.cschatbot.cs.infrastructure;

import com.capstone.cschatbot.cs.dto.response.Evaluation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Component
@HttpExchange
public interface EvaluationComponent {

    @GetExchange()
    Evaluation getEvaluation(
            @RequestParam String question, @RequestParam String answer
    );
}
