package com.codecat.catsurvey.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.models.Template;
import com.codecat.catsurvey.repository.TemplateRepository;
import com.codecat.catsurvey.service.TemplateService;
import com.codecat.catsurvey.service.UserService;
import com.codecat.catsurvey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/template")
@SaCheckLogin
@SaCheckPermission("TemplateManage")
@CrossOrigin
public class AdminTemplateController {
    @Autowired
    private TemplateService templateService;

    @PostMapping("")
    public Result add(@RequestBody @Validated({validationTime.FullAdd.class}) Template template) {
        templateService.add(template);
        return Result.success();
    }

    @PutMapping("/{templateId}")
    public Result modify(@PathVariable Integer templateId, @RequestBody Template template) {
        templateService.modify(templateId, template);
        return Result.success();
    }

    @DeleteMapping("/{templateId}")
    public Result del(@PathVariable Integer templateId) {
        templateService.del(templateId);
        return Result.success();
    }

    @GetMapping("")
    public Result getAll() {
        return Result.successData(templateService.getAll());
    }

    @GetMapping("/{templateId}")
    public Result get(@PathVariable Integer templateId) {
        return Result.successData(templateService.get(templateId));
    }
}
