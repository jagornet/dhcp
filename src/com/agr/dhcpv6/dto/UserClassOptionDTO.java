package com.agr.dhcpv6.dto;

import java.util.ArrayList;
import java.util.List;

public class UserClassOptionDTO extends BaseOptionDTO
{
    // TODO:  this probably won't work because when I rely on
    //        Dozer to do the magic reflection to perform the
    //        bean copy, the List<T> type doesn't actually
    //        match the server.config.xml.OpaqueData 
    //        non-DTO object which we want to convert...
    //        so, something bad is bound to happen
    protected List<OpaqueDataDTO> userClasses;

    public List<OpaqueDataDTO> getUserClasses()
    {
        return userClasses;
    }

    public void setUserClasses(List<OpaqueDataDTO> userClasses)
    {
        this.userClasses = userClasses;
    }

    public void addUserClass(OpaqueDataDTO userClass)
    {
        if (this.userClasses == null)
            this.userClasses = new ArrayList<OpaqueDataDTO>();
        userClasses.add(userClass);
    }
}
