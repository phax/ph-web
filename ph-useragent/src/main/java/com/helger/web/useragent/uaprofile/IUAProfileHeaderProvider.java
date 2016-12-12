package com.helger.web.useragent.uaprofile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.ext.ICommonsCollection;

public interface IUAProfileHeaderProvider
{
  @Nonnull
  ICommonsCollection <String> getAllHeaderNames ();

  @Nonnull
  ICommonsCollection <String> getHeaders (@Nullable String sName);

  @Nullable
  String getHeader (@Nullable String sName);
}
