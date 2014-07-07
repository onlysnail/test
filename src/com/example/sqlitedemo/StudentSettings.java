package com.example.sqlitedemo;

import android.net.Uri;
import android.provider.BaseColumns;

class StudentSettings {

	static interface BaseStudentColumns extends BaseColumns {

		static final String Name = "name";

		static final String Age = "age";
	}

	static final class Students implements BaseStudentColumns {
		/**
		 * The content:// style URL for this table
		 * 
		 * 数据库发生变化 > 发送通知
		 */
		static final Uri CONTENT_URI = Uri.parse("content://"
				+ StudentProvider.AUTHORITY + "/"
				+ StudentProvider.TABLE_STUDENTS + "?"
				+ StudentProvider.PARAMETER_NOTIFY + "=true");

		/**
		 * The content:// style URL for this table. When this Uri is used, no
		 * notification is sent if the content changes.
		 * 
		 * 数据库发生变化 > 不发送通知
		 */
		static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://"
				+ StudentProvider.AUTHORITY + "/"
				+ StudentProvider.TABLE_STUDENTS + "?"
				+ StudentProvider.PARAMETER_NOTIFY + "=false");

		/**
		 * The content:// style URL for a given row, identified by its id.
		 * 
		 * @param id
		 *            The row id.
		 * @param notify
		 *            True to send a notification is the content changes.
		 * 
		 * @return The unique content URL for the specified row.
		 * 
		 *         通过Id获取URI
		 */
		static Uri getContentUri(long id, boolean notify) {
			return Uri.parse("content://" + StudentProvider.AUTHORITY + "/"
					+ StudentProvider.TABLE_STUDENTS + "/" + id + "?"
					+ StudentProvider.PARAMETER_NOTIFY + "=" + notify);
		}
	}
}