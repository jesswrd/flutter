// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef FLUTTER_SHELL_PLATFORM_ANDROID_ANDROID_SURFACE_GL_H_
#define FLUTTER_SHELL_PLATFORM_ANDROID_ANDROID_SURFACE_GL_H_

#include <jni.h>
#include <memory>

#include "flutter/fml/macros.h"
#include "flutter/shell/gpu/gpu_surface_gl.h"
#include "flutter/shell/platform/android/android_context_gl.h"
#include "flutter/shell/platform/android/android_environment_gl.h"
#include "flutter/shell/platform/android/android_surface.h"
#include "flutter/shell/platform/android/external_view_embedder/external_view_embedder.h"
#include "flutter/shell/platform/android/jni/platform_view_android_jni.h"

namespace flutter {

class AndroidSurfaceGL final : public GPUSurfaceGLDelegate,
                               public AndroidSurface {
 public:
  AndroidSurfaceGL(std::shared_ptr<AndroidContext> android_context,
                   std::shared_ptr<PlatformViewAndroidJNI> jni_facade);

  ~AndroidSurfaceGL() override;

  // |AndroidSurface|
  bool IsValid() const override;

  // |AndroidSurface|
  std::unique_ptr<Surface> CreateGPUSurface(GrContext* gr_context) override;

  // |AndroidSurface|
  void TeardownOnScreenContext() override;

  // |AndroidSurface|
  bool OnScreenSurfaceResize(const SkISize& size) override;

  // |AndroidSurface|
  bool ResourceContextMakeCurrent() override;

  // |AndroidSurface|
  bool ResourceContextClearCurrent() override;

  // |AndroidSurface|
  bool SetNativeWindow(fml::RefPtr<AndroidNativeWindow> window) override;

  // |GPUSurfaceGLDelegate|
  std::unique_ptr<GLContextResult> GLContextMakeCurrent() override;

  // |GPUSurfaceGLDelegate|
  bool GLContextClearCurrent() override;

  // |GPUSurfaceGLDelegate|
  bool GLContextPresent() override;

  // |GPUSurfaceGLDelegate|
  intptr_t GLContextFBO() const override;

  // |GPUSurfaceGLDelegate|
  ExternalViewEmbedder* GetExternalViewEmbedder() override;

 private:
  fml::RefPtr<AndroidNativeWindow> native_window_;
  std::unique_ptr<AndroidExternalViewEmbedder> external_view_embedder_;
  std::shared_ptr<AndroidContextGL> android_context_;
  std::unique_ptr<AndroidEGLSurface> onscreen_surface_;
  std::unique_ptr<AndroidEGLSurface> offscreen_surface_;

  FML_DISALLOW_COPY_AND_ASSIGN(AndroidSurfaceGL);
};

}  // namespace flutter

#endif  // FLUTTER_SHELL_PLATFORM_ANDROID_ANDROID_SURFACE_GL_H_
