package pers.husen.demo.shiro.dao.impl;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import pers.husen.demo.shiro.dao.RoleDao;
import pers.husen.demo.shiro.po.SysRoles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Desc 带数据库连接池
 *
 * @Author 何明胜
 *
 * @Created at 2018年3月28日 上午11:26:45
 * 
 * @Version 1.0.0
 */
public class RoleDaoImpl extends JdbcDaoSupport implements RoleDao {

	public SysRoles createRole(final SysRoles Role) {
		final String sql = "insert into sys_roles(role, description, available) values(?,?,?)";

		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement psst = connection.prepareStatement(sql, new String[] { "id" });
				psst.setString(1, Role.getRole());
				psst.setString(2, Role.getDescription());
				psst.setBoolean(3, Role.getAvailable());
				return psst;
			}
		}, keyHolder);
		Role.setId(keyHolder.getKey().longValue());

		return Role;
	}

	public void deleteRole(Long roleId) {
		// 首先把和role关联的相关表数据删掉
		String sql = "delete from sys_users_roles where role_id=?";
		getJdbcTemplate().update(sql, roleId);

		sql = "delete from sys_roles where id=?";
		getJdbcTemplate().update(sql, roleId);
	}

	@Override
	public void correlationPermissions(Long roleId, Long... permissionIds) {
		if (permissionIds == null || permissionIds.length == 0) {
			return;
		}
		String sql = "insert into sys_roles_permissions(role_id, permission_id) values(?,?)";
		for (Long permissionId : permissionIds) {
			if (!exists(roleId, permissionId)) {
				getJdbcTemplate().update(sql, roleId, permissionId);
			}
		}
	}

	@Override
	public void uncorrelationPermissions(Long roleId, Long... permissionIds) {
		if (permissionIds == null || permissionIds.length == 0) {
			return;
		}
		String sql = "delete from sys_roles_permissions where role_id=? and permission_id=?";
		for (Long permissionId : permissionIds) {
			if (exists(roleId, permissionId)) {
				getJdbcTemplate().update(sql, roleId, permissionId);
			}
		}
	}

	private boolean exists(Long roleId, Long permissionId) {
		String sql = "select count(1) from sys_roles_permissions where role_id=? and permission_id=?";
		return getJdbcTemplate().queryForObject(sql, Integer.class, roleId, permissionId) != 0;
	}
}