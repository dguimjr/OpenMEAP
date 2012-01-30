/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.model.dto;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.openmeap.model.AbstractModelEntity;
import com.openmeap.model.ModelEntity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Do not instantiate this, get it from the ModelManager
 * @author schang
 */
@Entity @Table(name="global_settings")
public class GlobalSettings extends AbstractModelEntity {
	private Long id = null;
	private Map<String,ClusterNode> clusterNodes = null;
	private String temporaryStoragePath = null;
	private String serviceManagementAuthSalt = null;
	private Integer maxFileUploadSize = 1000000;
	
	/**
	 * This is the external url which is used as the root for uploaded archives.
	 * It should be externally accessible from the organization,
	 * as devices will be connecting to it to download new versions of the software.
	 */
	private String externalServiceUrlPrefix = null;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override public int hashCode() {
		return id!=null?id.intValue():0;
	}
	
	@Transient public Long getPk() { return getId(); }
	public void setPk( Object pkValue ) { setId((Long)pkValue); }
	
	@Column(name="external_svc_url")
	public String getExternalServiceUrlPrefix() {
		return externalServiceUrlPrefix;
	}
	public void setExternalServiceUrlPrefix(String externalServiceUrlPrefix) {
		this.externalServiceUrlPrefix = externalServiceUrlPrefix;
	}
	
	@Column(name="max_file_upload_size")
	public Integer getMaxFileUploadSize() {
		return maxFileUploadSize;
	}
	public void setMaxFileUploadSize(Integer size) {
		maxFileUploadSize = size;		
	}
	
	@OneToMany(fetch=FetchType.EAGER,cascade={CascadeType.ALL},targetEntity=ClusterNode.class)
	@MapKey(name="serviceWebUrlPrefix")
	@Lob
	public Map<String,ClusterNode> getClusterNodes() {
		return clusterNodes;
	}
	public void setClusterNodes(Map<String,ClusterNode> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}
	
	@Column(name="temp_strg_path",length=4000)
	public String getTemporaryStoragePath() {
		return temporaryStoragePath;
	}
	public void setTemporaryStoragePath(String temporaryStoragePath) {
		this.temporaryStoragePath = temporaryStoragePath;
	}
	public String validateTemporaryStoragePath() {
		if( temporaryStoragePath==null ) {
			return "Temporary storage path should be set";
		}
		File path = new File(temporaryStoragePath);
		List<String> errors = new ArrayList<String>();
		if( ! path.exists() ) {
			errors.add("does not exist");
		} else {
			if( ! path.canWrite() ) {
				return "not writable";
			}
			if( ! path.canRead() ) {
				return "not readable";
			}
		}
		if( errors.size()>0 ) {
			StringBuilder sb = new StringBuilder("The path \""+temporaryStoragePath+"\" has the following issues: ");
			sb.append(StringUtils.join(errors,","));
			return sb.toString();
		}
		return null;
	}
	
	@Column(name="svc_mgmt_auth_salt",length=4000)
	public String getServiceManagementAuthSalt() {
		return serviceManagementAuthSalt;
	}
	public void setServiceManagementAuthSalt(String serviceManagementAuthSalt) {
		this.serviceManagementAuthSalt = serviceManagementAuthSalt;
	}
	
	public Map<Method,String> validate() {
		try {
			Map<Method,String> errors = new HashMap<Method,String>();
			String error = validateTemporaryStoragePath();
			if( error!=null ) {
				errors.put(this.getClass().getMethod("getTemporaryStoragePath"),error);
			}
			if( errors.size()>0 )
				return errors;
			return null;
		} catch( NoSuchMethodException nsme ) {
			throw new RuntimeException(nsme);
		}
	}
}