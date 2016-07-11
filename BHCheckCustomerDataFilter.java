package com.dcits.supervise.aml.filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dcits.platform.system.Constant;
import com.dcits.platform.system.model.Org;
import com.dcits.platform.system.model.UserSession;
import com.dcits.platform.system.service.OrgService;
import com.dcits.platform.util.SpringUtil;
import com.dcits.springcrud.datafilter.DataFilter;
import com.dcits.springcrud.util.StringUtils;
import com.dcits.supervise.common.model.ViseOrg;
import com.dcits.supervise.common.service.ViseOrgService;
/**
 * 
 * 复核大额对应的客户信息filter
 *
 */
public class BHCheckCustomerDataFilter implements DataFilter{
	public String getFitlerWhereClause(HttpServletRequest request){
	
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(" 1=1  ");	
		/** 获取数据日期**/
		String dade = request.getParameter("dade");
		/** 获取机构代码**/
		String orgCode = request.getParameter("orgCode");	
		/**获取客户号**/
		String csnm = request.getParameter("csnm");
		/**获取客户名称**/
		String ctnm = request.getParameter("ctnm");
		/**获取注入的service*/
		OrgService orgService = (OrgService) SpringUtil.getBean("orgService");
		ViseOrgService viseOrgService = (ViseOrgService) SpringUtil.getBean("viseOrgService");
		/**
		 * 
		 * 默认查询当前登陆用户所在机构及下属机构下的信息
		 * 
		 */
		List<Org> orgList = new ArrayList<Org>();
		/**
		 * 存储当前登录用户所在法人机构及下属法人机构代码
		 */
		List<ViseOrg> companyList = new ArrayList<ViseOrg>();
		/**获取用户session**/
		UserSession userSession = (UserSession)request.getSession().getAttribute("userInfo");
		/**获取用户编号**/
		String userCode = userSession.getUser().getUserCode();
		try {
			/**admin用户**/
			if(userCode.equals(Constant.SYS_DEFAULT_USER) && (orgCode==null ||orgCode.equals(""))){
				orgList = orgService.findAllOrgs();
			}else{
				if(orgCode==null ||orgCode.equals("")){
					orgCode = userSession.getOrg().getOrgCode();
				}
				/**非admin用户**/
				orgList = orgService.getAllOrgsInfoForOrgCode(orgCode);
			}
			//查询法人机构代码
			Map<String, String> map = new HashMap<String, String>();
			map.put("company", userSession.getOrg().getCompany());
			map.put("userCode", userCode);
			companyList = viseOrgService.getCompanyInfo(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuffer orgIds = new StringBuffer();
		if (!orgList.isEmpty() && orgList.size()>0) {
			orgIds.append("(");
			for (int i = 0; i < orgList.size(); i++) {
				Org org = orgList.get(i);
				orgIds.append("'");
				orgIds.append(org.getOrgCode());
				orgIds.append("'");
				if (i != orgList.size() - 1) {
					orgIds.append(",");
				}
			}
			orgIds.append(")");
			sb.append(" AND FINC IN " + orgIds.toString());
		} 
		//构建法人字符串，便于查询多个法人机构数据
		if (!companyList.isEmpty() && companyList.size()>0) {
			StringBuffer companySB = new StringBuffer();
			companySB.append("(");
			for (int i = 0; i < companyList.size(); i++) {
				ViseOrg viseOrg = companyList.get(i);
				companySB.append("'");
				//sql中company的别名为orgCode,因此此处获取orgCode
				companySB.append(viseOrg.getOrgCode());
				companySB.append("'");
				if (i != companyList.size() - 1) {
					companySB.append(",");
				}
			}
			companySB.append(")");
			sb.append(" AND COMPANY IN " + companySB.toString());
		} 
		if (!StringUtils.isEmpty(dade)){
				sb.append(" AND DADE ='"+dade+"'");
		}
		if (!StringUtils.isEmpty(csnm)){
			sb.append(" AND CSNM LIKE '%"+csnm+"%'");
		}
		if (!StringUtils.isEmpty(ctnm)){
			sb.append(" AND CTNM LIKE '%"+ctnm+"%'");
		}
		return sb.toString();
	}
	public DataFilter setChainedFilter(DataFilter chainedFilter) {
		// TODO Auto-generated method stub
		return null;
	}
}
