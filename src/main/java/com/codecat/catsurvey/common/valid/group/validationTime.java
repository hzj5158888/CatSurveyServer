package com.codecat.catsurvey.common.valid.group;

import jakarta.validation.groups.Default;

public class validationTime {
    public interface Add extends Default {}

    public interface FullAdd extends Default {}

    public interface Update extends Default {}

    public interface FullUpdate extends Default {}

    public interface UpdatePassWord extends Default {}

    public interface UpdateLoginName extends Default {}
}
