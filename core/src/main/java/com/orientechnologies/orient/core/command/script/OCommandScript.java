/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.command.script;

import javax.script.CompiledScript;

import com.orientechnologies.common.io.OIOUtils;
import com.orientechnologies.orient.core.command.OCommandRequestTextAbstract;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.serialization.OMemoryStream;
import com.orientechnologies.orient.core.serialization.OSerializableStream;

/**
 * Script command request implementation. It just stores the request and delegated the execution to the configured OCommandExecutor.
 * 
 * 
 * @see OCommandExecutorScript
 * @author Luca Garulli
 * 
 */
@SuppressWarnings("serial")
public class OCommandScript extends OCommandRequestTextAbstract {
  private String         language;
  private CompiledScript compiledScript;

  public OCommandScript() {
    useCache = true;
  }

  public OCommandScript(final String iLanguage, final String iText) {
    super(iText);
    language = iLanguage;
    useCache = true;
  }

  public OCommandScript(final String iText) {
    super(iText);
  }

  public boolean isIdempotent() {
    return false;
  }

  public String getLanguage() {
    return language;
  }

  public OCommandScript setLanguage(String language) {
    this.language = language;
    return this;
  }

  public OSerializableStream fromStream(byte[] iStream) throws OSerializationException {
    final OMemoryStream buffer = new OMemoryStream(iStream);
    language = buffer.getAsString();
    fromStream(buffer);
    return this;
  }

  public byte[] toStream() throws OSerializationException {
    final OMemoryStream buffer = new OMemoryStream();
    buffer.set(language);
    return toStream(buffer);
  }

  public void setCompiledScript(CompiledScript script) {
    compiledScript = script;
  }

  public CompiledScript getCompiledScript() {
    return compiledScript;
  }

  @Override
  public String toString() {
    if (language != null)
      return language + "." + OIOUtils.getStringMaxLength(text, 200, "...");
    return "script." + OIOUtils.getStringMaxLength(text, 200, "...");
  }
}
