package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.codecat.catsurvey.models.Response;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.service.ResponseService;
import com.codecat.catsurvey.service.SurveyService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/analysis")
@SaCheckLogin
@SaCheckPermission("DataManage")
@CrossOrigin()
public class AnalysisController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private SurveyService surveyService;

    @GetMapping("")
    public Result get() {
        List<Survey> resSurveyList = new ArrayList<>();
        List<Response> resResponseList = new ArrayList<>();

        List<Survey> surveyList = surveyService.getAll();
        for (Survey survey : surveyList) {
            LocalDate now = LocalDate.now();
            LocalDate createDate = Instant.ofEpochMilli(survey.getCreateDate().getTime())
                                            .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
            if (now.getMonthValue() != createDate.getMonthValue())
                break;

            resSurveyList.add(survey);
        }

        List<Response> responseList = responseService.getAll();
        for (Response response : responseList) {
            LocalDate now = LocalDate.now();
            LocalDate createDate = Instant.ofEpochMilli(response.getSubmitDate().getTime())
                    .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
            if (now.getMonthValue() != createDate.getMonthValue())
                break;

            resResponseList.add(response);
        }

        Map<String, Object> ans = new HashMap<>();
        ans.put("surveyList", resSurveyList);
        ans.put("responseList", resResponseList);
        return Result.successData(ans);
    }
}
