package edu.tomm.webreporter.service.mapper;

import edu.tomm.webreporter.domain.Report;
import edu.tomm.webreporter.domain.User;
import edu.tomm.webreporter.service.dto.ReportDTO;
import edu.tomm.webreporter.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Report} and its DTO {@link ReportDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReportMapper extends EntityMapper<ReportDTO, Report> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    ReportDTO toDto(Report s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
