%define _unpackaged_files_terminate_build 0
%define _binaries_in_noarch_packages_terminate_build 0
%define __jar_repack 0
Name: graylog-plugin-zabbix
Version: 0.0.1
Release: 1
Summary: graylog-plugin-zabbix
License: (c) null
Group: Application/Internet
autoprov: yes
autoreq: yes
Prefix: /usr
BuildArch: noarch
BuildRoot: /home/905097/repos/graylog-plugin-zabbix/target/rpm/graylog-plugin-zabbix/buildroot

%description
Graylog Zabbix trapper Alarmcallback plugin

%install

if [ -d $RPM_BUILD_ROOT ];
then
  mv /home/905097/repos/graylog-plugin-zabbix/target/rpm/graylog-plugin-zabbix/tmp-buildroot/* $RPM_BUILD_ROOT
else
  mv /home/905097/repos/graylog-plugin-zabbix/target/rpm/graylog-plugin-zabbix/tmp-buildroot $RPM_BUILD_ROOT
fi

%files
%defattr(644,root,root,755)
 "/usr/share/graylog-server/plugin"
