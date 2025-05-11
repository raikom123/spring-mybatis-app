package mrs.domain.model;

import mrs.config.mybatis.MybatisMappedValue;

public enum RoleName implements MybatisMappedValue {
    ADMIN,
    USER;

    @Override
    public String getValue() {
        return name();
    }
}
