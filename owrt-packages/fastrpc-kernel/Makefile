include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=fastrpc-kernel
PKG_VERSION:=1.0
PKG_RELEASE:=1
PKG_FIXUP:=autoreconf
PKG_LICENSE:=GPL-2.0-only

LOCAL_SRC := $(TOPDIR)/src/vendor/qcom/opensource/dsp-kernel

include $(INCLUDE_DIR)/package.mk

define KernelPackage/$(PKG_NAME)
  SECTION:=Kernel Modules
  CATEGORY:=External Kernel Modules
  TITLE:=Driver for fastrpc
endef

define KernelPackage/$(PKG_NAME)/Description
	This package contains a driver for fastrpc.
endef

define Build/Prepare
	mkdir -p $(PKG_BUILD_DIR)/
	$(CP) $(LOCAL_SRC)/* $(PKG_BUILD_DIR)/
	$(Build/Patch)
endef

MAKE_OPTS:= \
    M="$(PKG_BUILD_DIR)"


define Build/Compile
	$(MAKE) -C "$(LINUX_DIR)" \
	$(MAKE_OPTS)\
	modules
endef

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/include/linux
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/include/linux/fastrpc.h $(1)/usr/include/linux/
endef

define KernelPackage/$(PKG_NAME)/install
#	$(SIGN_KEY) $(PKG_BUILD_DIR)/frpc-adsprpc.ko
	$(INSTALL_DIR) $(1)/lib/modules/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/frpc-adsprpc.ko $(1)/lib/modules/
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_DIR) $(1)/etc/initscripts/
	$(INSTALL_BIN) ./files/dsp.init $(1)/etc/init.d
	$(INSTALL_BIN) ./files/start_dsp_le $(1)/etc/initscripts/

#install ko's in /bin/targets/$(TARGET_VARIANT)/modules to be shown in owrt CRM APPS
	$(INSTALL_DIR) $(TOPDIR)/bin/targets/$(TARGET_VARIANT)/modules
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/frpc-adsprpc.ko $(TOPDIR)/bin/targets/$(TARGET_VARIANT)/modules/
endef

$(eval $(call KernelPackage,$(PKG_NAME)))
