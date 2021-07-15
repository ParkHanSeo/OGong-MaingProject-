package com.ogong.service.study;

import java.util.HashMap;
import java.util.Map;

import com.ogong.common.Search;
import com.ogong.service.domain.GroupStudyMember;
import com.ogong.service.domain.Study;

public interface StudyService {
	
	
	public void addStudy(Study study) throws Exception;
	
	public Study getStudy(int studyNo) throws Exception;
	
	public Map<String, Object> getStudyList(HashMap<String, Object> map) throws Exception;
	
	public void deleteStudy(int studyNo) throws Exception;
	
	public void addParticipation(GroupStudyMember gsm) throws Exception;
	
	}
