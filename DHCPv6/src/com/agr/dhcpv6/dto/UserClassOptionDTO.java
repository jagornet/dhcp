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
    //
    // NB:  actually, Dozer DOES convert the xml.OpaqueData
    //      objects to OpaqueDataDTO objects, but only if
    //      we use Java 5 Generics to specify the type of
    //      object in the List<OpaqueDataDTO>; however, we
    //      can't use generics in the DTOs yet, because
    //      GWT won't support them until v1.5
//  We can't use Java 5 Generics with GWT yet
//    protected List<OpaqueDataDTO> userClasses;
    protected List userClasses;

//    public List<OpaqueDataDTO> getUserClasses()
    public List getUserClasses()
    {
        return userClasses;
    }

//    public void setUserClasses(List<OpaqueDataDTO> userClasses)
    public void setUserClasses(List userClasses)
    {
        this.userClasses = userClasses;
    }

    public void addUserClass(OpaqueDataDTO userClass)
    {
        if (this.userClasses == null)
//            this.userClasses = new ArrayList<OpaqueDataDTO>();
            this.userClasses = new ArrayList();
        userClasses.add(userClass);
    }
}
