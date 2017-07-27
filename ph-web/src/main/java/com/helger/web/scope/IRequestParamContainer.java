package com.helger.web.scope;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.attr.IMutableAttributeContainerAny;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.web.fileupload.IFileItem;

/**
 * A special request parameter map with support for file items etc.
 *
 * @author Philip Helger
 */
public interface IRequestParamContainer extends IMutableAttributeContainerAny <String>
{
  /**
   * @return A non-<code>null</code> but maybe empty map with all contained
   *         {@link IFileItem} objects from file uploads. The key of the map is
   *         the field name. Important: if the value is an array of
   *         {@link IFileItem} it is not considered in the returned map!
   */
  @Nonnull
  default ICommonsMap <String, IFileItem> getAllUploadedFileItems ()
  {
    final ICommonsMap <String, IFileItem> ret = new CommonsHashMap <> ();
    for (final Map.Entry <String, Object> aEntry : entrySet ())
    {
      final Object aAttrValue = aEntry.getValue ();
      if (aAttrValue instanceof IFileItem)
        ret.put (aEntry.getKey (), (IFileItem) aAttrValue);
    }
    return ret;
  }

  /**
   * @return A non-<code>null</code> but maybe empty map with all contained
   *         {@link IFileItem} objects from file uploads. The key of the map is
   *         the field name.
   */
  @Nonnull
  default ICommonsMap <String, IFileItem []> getAllUploadedFileItemsComplete ()
  {
    final ICommonsMap <String, IFileItem []> ret = new CommonsHashMap <> ();
    for (final Map.Entry <String, Object> aEntry : entrySet ())
    {
      final String sAttrName = aEntry.getKey ();
      final Object aAttrValue = aEntry.getValue ();
      if (aAttrValue instanceof IFileItem)
        ret.put (sAttrName, new IFileItem [] { (IFileItem) aAttrValue });
      else
        if (aAttrValue instanceof IFileItem [])
          ret.put (sAttrName, ArrayHelper.getCopy ((IFileItem []) aAttrValue));
    }
    return ret;
  }

  /**
   * @return A non-<code>null</code> but maybe empty list of all
   *         {@link IFileItem} objects in the request. In comparison to
   *         {@link #getAllUploadedFileItems()} this method also returns the
   *         content of {@link IFileItem} arrays.
   */
  @Nonnull
  default ICommonsList <IFileItem> getAllUploadedFileItemValues ()
  {
    final ICommonsList <IFileItem> ret = new CommonsArrayList <> ();
    for (final Object aAttrValue : values ())
    {
      if (aAttrValue instanceof IFileItem)
        ret.add ((IFileItem) aAttrValue);
      else
        if (aAttrValue instanceof IFileItem [])
          for (final IFileItem aChild : (IFileItem []) aAttrValue)
            ret.add (aChild);
    }
    return ret;
  }

  /**
   * Get the request attribute denoted by the specified attribute name as an
   * uploaded file item. In case the specified parameter is present but not a
   * file item, the method returns <code>null</code>.
   *
   * @param sAttrName
   *        The attribute name to resolved. May be <code>null</code>.
   * @return <code>null</code> if no such attribute is present, or if the
   *         attribute is not a file item.
   */
  @Nullable
  default IFileItem getAsFileItem (@Nullable final String sAttrName)
  {
    return getSafeCastedValue (sAttrName, IFileItem.class);
  }
}
